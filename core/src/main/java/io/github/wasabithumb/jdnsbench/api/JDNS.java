package io.github.wasabithumb.jdnsbench.api;

import io.github.wasabithumb.jdnsbench.api.address.Address;
import io.github.wasabithumb.jdnsbench.api.address.AddressType;
import io.github.wasabithumb.jdnsbench.jni.JNI;
import io.github.wasabithumb.jdnsbench.jni.JNIQueryCallback;
import io.github.wasabithumb.jdnsbench.util.SystemUtil;
import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class JDNS implements Closeable {

    static final JNI jni = new JNI();
    static int jniInitCounter = 0;

    public JDNS() {
        synchronized (jni) {
            if ((jniInitCounter++) == 0) {
                long err = jni.init();
                if (err != 0L) throw new JDNSException(err);
            }
        }
    }

    public @NotNull CompletableFuture<JDNSResult> query(
            @NotNull Address nameserver,
            @NotNull String domain
    ) {
        if (SystemUtil.IS_WINDOWS && nameserver.type() == AddressType.V6) {
            throw new IllegalStateException("IPv6 nameservers are not currently supported on Windows");
        }
        CompletableFuture<JDNSResult> ret = new CompletableFuture<>();
        QueryCallback cb = new QueryCallback(ret);
        long code = jni.query(nameserver.address(), domain, cb);
        if (code != 0L) {
            ret.completeExceptionally(new JDNSException(code));
        }
        return ret;
    }

    public @NotNull JDNSResult querySync(
            @NotNull Address nameserver,
            @NotNull String domain
    ) throws JDNSException {
        CompletableFuture<JDNSResult> future = this.query(nameserver, domain);
        try {
            return future.get();
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof JDNSException) {
                throw (JDNSException) cause;
            } else {
                throw new AssertionError("Unexpected error", cause);
            }
        } catch (InterruptedException e) {
            throw new AssertionError("Unexpected interruption", e);
        }
    }

    @Override
    public void close() {
        synchronized (jni) {
            if ((--jniInitCounter) == 0) {
                long err = jni.destroy();
                if (err != 0L) throw new JDNSException(err);
            }
        }
    }

    //

    private static class QueryCallback implements JNIQueryCallback {

        private final CompletableFuture<JDNSResult> future;
        private final long timeSent;
        private long timeReceived = 0L;
        private long status = -1L;
        private List<Address> addresses = null;

        public QueryCallback(CompletableFuture<JDNSResult> future) {
            this.future = future;
            this.timeSent = System.nanoTime();
        }

        @Override
        public void open(long status, int timeouts) {
            this.timeReceived = System.nanoTime();
            this.status = status;
            if (status == 0L) this.addresses = new ArrayList<>(2);
        }

        @Override
        public void addV4(String v4) {
            if (this.status == 0L) this.addresses.add(Address.v4(v4));
        }

        @Override
        public void addV6(String v6) {
            if (this.status == 0L) this.addresses.add(Address.v6(v6));
        }

        @Override
        public void close() {
            if (this.status == 0L) {
                final long elapsed = this.timeReceived - this.timeSent;
                final JDNSResult result = new JDNSResult(elapsed, Collections.unmodifiableList(this.addresses));
                this.future.complete(result);
            } else {
                this.future.completeExceptionally(new JDNSException(this.status));
            }
        }

    }

}
