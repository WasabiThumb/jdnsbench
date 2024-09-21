package io.github.wasabithumb.jdnsbench.api.address.source;

import io.github.wasabithumb.jdnsbench.asset.loader.AssetLoader;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;

public class AssetCSVAddressSource extends CSVAddressSource {

    private final AssetLoader loader;
    private final String label;
    private final String path;
    public AssetCSVAddressSource(@NotNull AssetLoader loader, @NotNull String label, @NotNull String path) {
        this.loader = loader;
        this.label = label;
        this.path = path;
    }

    public AssetCSVAddressSource(@NotNull AssetLoader loader, @NotNull String path) {
        this(loader, "core/" + path, path);
    }

    @Override
    public @NotNull String label() {
        return this.label;
    }

    @Override
    protected @NotNull InputStream getStream() {
        InputStream ret = this.loader.loadStream(this.path);
        if (ret == null)
            throw new AssertionError("Asset \"" + this.path + "\" not found in loader: " + this.loader);
        return ret;
    }

    @Override
    protected @NotNull String getAddressKey() {
        return "ip";
    }

    @Override
    protected @NotNull String[] getLabelKeys() {
        return new String[] { "label" };
    }

    @Override
    protected int getExpectedColumns() {
        return 3;
    }

}
