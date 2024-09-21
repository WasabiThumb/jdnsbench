package io.github.wasabithumb.jdnsbench.api;

public class JDNSException extends RuntimeException {

    private final long code;
    JDNSException(final long code) {
        this.code = code;
    }

    public long getCode() {
        return this.code;
    }

    @Override
    public String getMessage() {
        return JDNS.jni.strerror(this.code);
    }

}
