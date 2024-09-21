package io.github.wasabithumb.jdnsbench.asset;

import io.github.wasabithumb.jdnsbench.asset.exception.AssetLoadException;
import io.github.wasabithumb.jdnsbench.asset.loader.AssetLoader;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;

import static io.github.wasabithumb.jdnsbench.util.ReflectUtil.getCodeSource;

public final class AppResources {

    private static final AssetLoader LOADER = AssetLoader.fromCodeSource(getCodeSource());

    //

    @Contract(pure = true)
    public static @NotNull AssetLoader getLoader() {
        return LOADER;
    }

    public static @NotNull InputStream getStreamAssert(final @NotNull String name) throws AssetLoadException, AssertionError {
        InputStream ret = LOADER.loadStream(name);
        if (ret == null) throw new AssertionError("Resource \"" + name + "\" not found");
        return ret;
    }

}
