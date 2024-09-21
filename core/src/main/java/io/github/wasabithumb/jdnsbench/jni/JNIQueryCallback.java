package io.github.wasabithumb.jdnsbench.jni;

public interface JNIQueryCallback {

    void open(long status, int timeouts);

    void addV4(String v4);

    void addV6(String v6);

    void close();

}
