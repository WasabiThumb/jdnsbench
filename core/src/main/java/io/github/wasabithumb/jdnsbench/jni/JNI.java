package io.github.wasabithumb.jdnsbench.jni;

import io.github.wasabithumb.jdnsbench.asset.CoreResources;
import io.github.wasabithumb.jdnsbench.util.SystemUtil;

public class JNI {

    static {
        if (SystemUtil.IS_LINUX) {
            System.loadLibrary("cares");
        }
        // Load the bundled native library
        CoreResources.loadLibraryAssert("jdnsbench");
    }

    public native String strerror(long code);

    public native long init();

    public native long destroy();

    public native long query(String server, String domain, JNIQueryCallback callback);

}
