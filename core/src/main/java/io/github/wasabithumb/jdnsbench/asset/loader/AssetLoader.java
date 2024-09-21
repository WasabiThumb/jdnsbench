package io.github.wasabithumb.jdnsbench.asset.loader;

import io.github.wasabithumb.jdnsbench.asset.exception.AssetLoadException;
import io.github.wasabithumb.jdnsbench.util.SystemUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.InputStream;

public abstract class AssetLoader {

    private static AssetLoader INSTANCE = null;

    public static @NotNull AssetLoader fromCodeSource(@NotNull File codeSource) {
        synchronized (AssetLoader.class) {
            if (INSTANCE != null) return INSTANCE;
            if (codeSource.isFile()) {
                INSTANCE = new JarAssetLoader(codeSource);
            } else {
                INSTANCE = new DevAssetLoader(codeSource);
            }
            return INSTANCE;
        }
    }

    // Interface

    public abstract boolean loadLibrary(@NotNull String name) throws AssetLoadException;

    public abstract @Nullable InputStream loadStream(@NotNull String name) throws AssetLoadException;

    // Helpers

    protected final @NotNull String nativeLibraryFileName(@NotNull String name) {
        if (SystemUtil.IS_WINDOWS) {
            return name + ".dll";
        } else {
            return "lib" + name + ".so";
        }
    }

}
