package io.github.wasabithumb.jdnsbench.api.bench;

import io.github.wasabithumb.jdnsbench.api.address.Address;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class JDNSBenchJob {

    private final int index;
    private final Address nameserver;
    private final int reps;
    private final long[] times;
    private State state = State.QUEUED;
    private int progress = 0;
    private String errorMessage = null;
    public JDNSBenchJob(final int index, final Address nameserver, final int reps) {
        if (reps < 0) throw new IllegalArgumentException("Reps must be positive");
        this.index = index;
        this.nameserver = nameserver;
        this.reps = reps;
        this.times = new long[reps];
    }

    public final int getIndex() {
        return this.index;
    }

    public final @NotNull Address getNameserver() {
        return this.nameserver;
    }

    public final int getReps() {
        return this.reps;
    }

    public final @NotNull State getState() {
        return this.state;
    }

    public final int getProgress() {
        return switch (this.state) {
            case QUEUED -> 0;
            case RUNNING -> this.progress;
            default -> this.reps;
        };
    }

    public final long getTime() {
        if (this.state == State.COMPLETE) {
            long sum = 0;
            for (long time : this.times) sum += time;
            return Math.floorDiv(sum, this.reps);
        }
        return (this.state == State.ERROR ? Long.MAX_VALUE - 1L : Long.MAX_VALUE);
    }

    public String getErrorMessage() {
        if (this.state == State.ERROR) return Objects.requireNonNull(this.errorMessage);
        return null;
    }

    @ApiStatus.Internal
    public void markRunning() {
        if (this.state != State.QUEUED) throw new IllegalStateException();
        if (this.reps == 0) {
            this.state = State.COMPLETE;
        } else {
            this.state = State.RUNNING;
        }
    }

    @ApiStatus.Internal
    public void submitTime(final long time) {
        if (this.state != State.RUNNING) throw new IllegalStateException();
        this.times[this.progress++] = time;
        if (this.progress == this.reps) {
            this.state = State.COMPLETE;
        }
    }

    @ApiStatus.Internal
    public void submitError(@Nullable final String errorMessage) {
        if (this.state == State.COMPLETE || this.state == State.ERROR) throw new IllegalStateException();
        this.errorMessage = Objects.requireNonNullElse(errorMessage, "Unknown error");
        this.state = State.ERROR;
    }

    @Contract(" -> new")
    public @NotNull JDNSBenchJob copy() {
        final JDNSBenchJob ret = new JDNSBenchJob(this.index, this.nameserver, this.reps);
        System.arraycopy(this.times, 0, ret.times, 0, this.progress);
        ret.state = this.state;
        ret.progress = this.progress;
        if (this.state == State.ERROR) ret.errorMessage = this.errorMessage;
        return ret;
    }

    //

    public enum State {
        QUEUED,
        RUNNING,
        COMPLETE,
        ERROR
    }

}
