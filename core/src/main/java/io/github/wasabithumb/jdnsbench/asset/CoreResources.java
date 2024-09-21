package io.github.wasabithumb.jdnsbench.asset;

import io.github.wasabithumb.jdnsbench.asset.exception.AssetLoadException;
import io.github.wasabithumb.jdnsbench.asset.loader.AssetLoader;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import static io.github.wasabithumb.jdnsbench.util.ReflectUtil.getCodeSource;

public final class CoreResources {

    private static final AssetLoader LOADER = AssetLoader.fromCodeSource(getCodeSource());

    //

    @Contract(pure = true)
    public static @NotNull AssetLoader getLoader() {
        return LOADER;
    }

    public static boolean loadLibrary(@NotNull String name) throws AssetLoadException {
        return LOADER.loadLibrary(name);
    }

    public static void loadLibraryAssert(@NotNull String name) throws AssetLoadException, AssertionError {
        if (!loadLibrary(name)) throw new AssertionError("Library \"" + name + "\" not found");
    }

}
