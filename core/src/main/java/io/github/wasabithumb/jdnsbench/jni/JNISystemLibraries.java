package io.github.wasabithumb.jdnsbench.jni;

import io.github.wasabithumb.jdnsbench.asset.CoreResources;
import io.github.wasabithumb.jdnsbench.asset.exception.AssetLoadException;
import io.github.wasabithumb.jdnsbench.util.SystemUtil;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class JNISystemLibraries {

    private static final boolean CARES_LOADED;
    private static final Throwable CARES_ERR;
    static {
        boolean caresLoaded = true;
        Throwable caresErr = null;

        if (SystemUtil.IS_LINUX) {
            try {
                System.loadLibrary("cares");
            } catch (UnsatisfiedLinkError e1) {
                try {
                    CoreResources.loadLibraryAssert("cares");
                } catch (AssetLoadException | AssertionError e2) {
                    e2.addSuppressed(e1);
                    caresErr = e2;
                    caresLoaded = false;
                }
            }
        }

        CARES_LOADED = caresLoaded;
        CARES_ERR = caresErr;
    }

    //

    public static boolean missingCares() {
        return !CARES_LOADED;
    }

    public static void unwrapCares() {
        if (CARES_LOADED) return;
        throw new RuntimeException("Failed to load C-ARES", CARES_ERR);
    }

}
