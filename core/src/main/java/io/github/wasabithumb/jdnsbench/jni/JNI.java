package io.github.wasabithumb.jdnsbench.jni;

import io.github.wasabithumb.jdnsbench.asset.CoreResources;

public class JNI {

    static {
        JNISystemLibraries.unwrapCares();
        CoreResources.loadLibraryAssert("jdnsbench");
    }

    public native String strerror(long code);

    public native long init();

    public native long destroy();

    public native long query(String server, String domain, JNIQueryCallback callback);

}
