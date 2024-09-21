package io.github.wasabithumb.jdnsbench.api.bench;

import io.github.wasabithumb.jdnsbench.api.JDNS;
import io.github.wasabithumb.jdnsbench.api.JDNSResult;
import io.github.wasabithumb.jdnsbench.api.address.Address;
import io.github.wasabithumb.jdnsbench.api.address.AddressType;
import io.github.wasabithumb.jdnsbench.api.address.source.AddressSource;
import io.github.wasabithumb.jdnsbench.util.collections.FixedIntArrayList;
import io.github.wasabithumb.jdnsbench.util.collections.FlatteningList;
import io.github.wasabithumb.jdnsbench.util.collections.FlipList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;
import org.jetbrains.annotations.Unmodifiable;

import java.io.Closeable;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.StampedLock;

public class JDNSBench implements Closeable {

    protected final JDNS runtime = new JDNS();
    protected final Executor executor;
    protected State state;
    protected String errorMessage = null;

    public JDNSBench(final JDNSBenchOptions opts, boolean autoStart) {
        this.executor = new Executor(this, opts);
        if (autoStart) {
            this.state = State.RESOLVING;
            this.executor.start();
        } else {
            this.state = State.IDLE;
        }
    }

    public JDNSBench(final JDNSBenchOptions opts) {
        this(opts, true);
    }

    public void start() {
        synchronized (this) {
            if (this.state != State.IDLE) return;
            this.state = State.RESOLVING;
            this.executor.start();
        }
    }

    public @NotNull State getState() {
        synchronized (this) {
            return this.state;
        }
    }

    public @NotNull @Unmodifiable List<JDNSBenchJob> getJobs(int offset, int limit, @NotNull JDNSBenchJobSort sort) {
        State state = this.getState();
        if (state == State.IDLE) return Collections.emptyList();
        if (state == State.RESOLVING) return Collections.emptyList();
        List<JDNSBenchJob> ret = this.executor.getJobs(offset, limit, sort.getCategory());
        if (sort.isReversed()) ret = new FlipList<>(ret);
        return ret;
    }

    public @NotNull @Unmodifiable List<JDNSBenchJob> getJobs(int offset, int limit) {
        return this.getJobs(offset, limit, JDNSBenchJobSort.STATE);
    }

    public int getJobCount() {
        State state = this.getState();
        if (state == State.IDLE || state == State.RESOLVING) return 0;
        return this.executor.getJobCount();
    }

    public double getProgress() {
        State state = this.getState();
        if (state == State.IDLE || state == State.RESOLVING) return 0d;
        if (state.isDone()) return 1d;
        return this.executor.getProgress();
    }

    protected void setState(final @NotNull State state) {
        if (state == State.ERROR) throw new IllegalArgumentException();
        synchronized (this) {
            this.state = state;
        }
    }

    protected void raise(final @NotNull Throwable t) {
        String message = t.getMessage();
        if (message == null) message = "Unknown error of type " + t.getClass().getName();
        synchronized (this) {
            this.errorMessage = message;
            this.state = State.ERROR;
        }
    }

    public @UnknownNullability String getErrorMessage() {
        synchronized (this) {
            return this.errorMessage;
        }
    }

    public void stop() {
        synchronized (this) {
            if (this.state == State.IDLE) return;
            if (this.state != State.COMPLETE) this.state = State.INTERRUPTED;
        }
        if (!this.executor.isAlive()) return;
        try {
            this.executor.interrupt();
            this.executor.join();
        } catch (InterruptedException ignored) { }
    }

    @Override
    public void close() {
        this.stop();
        this.runtime.close();
    }

    //

    public enum State {
        IDLE,
        RESOLVING,
        RUNNING,
        COMPLETE,
        INTERRUPTED,
        ERROR;

        public boolean isDone() {
            if (this == RUNNING) return false;
            return this != RESOLVING;
        }
    }

    //

    protected static class Executor extends Thread {

        private final JDNSBench bench;
        private final AtomicReference<JDNSBenchOptions> opts;
        private final String domain;
        private final long period;
        private final long timeout;
        private final boolean useV6;
        private final StampedLock jobsLock = new StampedLock();
        private List<JDNSBenchJob> jobs = null;
        private boolean jobsInit = false;
        private int jobActive = 0;
        private int[] jobSortLabel = null;
        private int[] jobSortTime = null;
        private boolean jobSortTimeValid = false;
        private final Lock jobSortLock = new ReentrantLock();

        Executor(JDNSBench bench, JDNSBenchOptions opts) {
            super("JDNSBench Executor");
            this.bench = bench;
            this.opts = new AtomicReference<>(opts);
            this.domain = opts.domain();
            this.period = opts.period();
            this.timeout = opts.timeout();
            this.useV6 = opts.useV6();
        }

        public double getProgress() {
            long stamp = this.jobsLock.readLock();
            try {
                if (!this.jobsInit) return 0d;

                int size = this.jobs.size();
                int count = this.jobActive;

                final JDNSBenchJob active = this.jobs.get(this.jobActive);
                size *= active.getReps();
                count = (count * active.getReps()) + active.getProgress();

                return ((double) count) / ((double) size);
            } finally {
                this.jobsLock.unlock(stamp);
            }
        }

        public @NotNull @Unmodifiable List<JDNSBenchJob> getJobs(int offset, int limit, @NotNull JDNSBenchJobSort.Category sort) {
            if (offset < 0) return Collections.emptyList();
            long stamp = this.jobsLock.readLock();
            try {
                if (!this.jobsInit) return Collections.emptyList();
                final int count = this.jobs.size();
                if (offset >= count) return Collections.emptyList();

                final int size = Math.min(offset + limit, count) - offset;
                JDNSBenchJob[] ret = new JDNSBenchJob[size];

                this.jobSortLock.lock();
                try {
                    for (int i = 0; i < size; i++) ret[i] = this.getJob(offset + i, sort).copy();
                } finally {
                    this.jobSortLock.unlock();
                }

                //noinspection Java9CollectionFactory
                return Collections.unmodifiableList(Arrays.asList(ret));
            } finally {
                this.jobsLock.unlock(stamp);
            }
        }

        /** MUST BE READ-LOCKED */
        private @NotNull JDNSBenchJob getJob(int index, JDNSBenchJobSort.Category sort) {
            int[] indices;
            switch (sort) {
                case STATE:
                    final int active = this.jobActive;
                    if (index > active) break;
                    index = active - index;
                    break;
                case LABEL:
                    indices = this.jobSortLabel;
                    if (indices == null) {
                        indices = new int[this.jobs.size()];
                        for (int i=0; i < this.jobs.size(); i++) indices[i] = i;
                        this.sortJobIndices(indices, Comparator.comparing(JDNSBenchJob::getNameserver));
                        this.jobSortLabel = indices;
                    }
                    index = indices[index];
                    break;
                case TIME:
                    indices = this.jobSortTime;
                    if (!this.jobSortTimeValid) {
                        if (indices == null) {
                            indices = new int[this.jobs.size()];
                            for (int i=0; i < this.jobs.size(); i++) indices[i] = i;
                            this.jobSortTime = indices;
                        }
                        this.sortJobIndices(indices, Comparator.comparingLong(JDNSBenchJob::getTime));
                        this.jobSortTimeValid = true;
                    }
                    index = indices[index];
                    break;
            }

            return this.jobs.get(index);
        }

        private void sortJobIndices(int[] indices, final Comparator<JDNSBenchJob> comparator) {
            final List<JDNSBenchJob> jobs = this.jobs;
            FixedIntArrayList list = new FixedIntArrayList(indices);
            list.sort((integer, t1) -> comparator.compare(
                    jobs.get(integer),
                    jobs.get(t1)
            ));
        }

        public int getJobCount() {
            long stamp = this.jobsLock.readLock();
            try {
                return this.jobs.size();
            } finally {
                this.jobsLock.unlock(stamp);
            }
        }

        @Override
        public void run() {
            try {
                this.run0();
                this.bench.setState(JDNSBench.State.COMPLETE);
            } catch (InterruptedException ignored) {
            } catch (Exception e2) {
                this.bench.raise(e2);
            }
        }

        private void run0() throws Exception {
            long stamp;
            List<JDNSBenchJob> jobs = this.resolveJobs();
            stamp = this.jobsLock.writeLock();
            try {
                this.jobs = jobs;
                this.jobsInit = true;
            } finally {
                this.jobsLock.unlock(stamp);
            }
            this.bench.setState(JDNSBench.State.RUNNING);

            final int count = jobs.size();
            JDNSBenchJob job;
            for (int i=0; i < count; i++) {
                job = jobs.get(i);
                boolean first = true;
                int reps = job.getReps();
                for (int q=0; q < reps; q++) {
                    stamp = first ? this.jobsLock.writeLock() : this.jobsLock.readLock();
                    try {
                        if (first) {
                            job.markRunning();
                            this.jobActive = i;
                            stamp = this.jobsLock.tryConvertToReadLock(stamp);
                        }

                        CompletableFuture<JDNSResult> future = this.bench.runtime.query(
                                job.getNameserver(),
                                this.domain
                        );
                        JDNSResult result = future.get(this.timeout, TimeUnit.MILLISECONDS);

                        stamp = this.jobsLock.tryConvertToWriteLock(stamp);
                        job.submitTime(result.elapsed());
                    } catch (TimeoutException e1) {
                        stamp = this.jobsLock.tryConvertToWriteLock(stamp);
                        job.submitError("Timed out");
                        q = (reps - 1);
                    } catch (InterruptedException e2) {
                        throw e2;
                    } catch (Exception e) {
                        stamp = this.jobsLock.tryConvertToWriteLock(stamp);
                        job.submitError(e.getMessage());
                        q = (reps - 1);
                    } finally {
                        this.jobsLock.unlock(stamp);
                        first = false;
                    }
                    if (!(q == (reps - 1) && i == (count - 1))) {
                        TimeUnit.MILLISECONDS.sleep(this.period);
                        if (this.bench.getState() == JDNSBench.State.INTERRUPTED) return;
                    }
                }

                this.jobSortLock.lock();
                try {
                    this.jobSortTimeValid = false;
                } finally {
                    this.jobSortLock.unlock();
                }
            }
        }

        private List<JDNSBenchJob> resolveJobs() throws IOException {
            final JDNSBenchOptions opts = this.opts.getAndSet(null);
            final Collection<Address> nameservers = opts.nameservers();
            final Collection<AddressSource> sources = opts.nameserverSources();
            final int reps = opts.reps();
            int jobIndex = 0;

            final List<JDNSBenchJob> jobs = new ArrayList<>(nameservers.size());
            for (final Address addr : nameservers) {
                if (!this.useV6 && addr.type() == AddressType.V6) continue;
                jobs.add(new JDNSBenchJob(jobIndex++, addr, reps));
            }

            final int sourceCount = sources.size();
            if (sourceCount == 0) {
                return jobs;
            }

            final List<List<JDNSBenchJob>> compound = new ArrayList<>(sourceCount + 1);
            compound.add(jobs);

            Collection<Address> col;
            for (AddressSource source : sources) {
                try {
                    col = source.get();
                } catch (IOException e) {
                    throw new IOException("Error fetching address source (" + source.label() + ")", e);
                }

                List<JDNSBenchJob> mapped = new ArrayList<>(col.size());
                for (Address addr : col) {
                    if (!this.useV6 && addr.type() == AddressType.V6) continue;
                    mapped.add(new JDNSBenchJob(jobIndex++, addr, reps));
                }
                compound.add(mapped);
            }

            return new FlatteningList<>(compound);
        }

    }

}
