package io.github.wasabithumb.jdnsbench.gui.asset;

import io.github.wasabithumb.jdnsbench.asset.exception.AssetLoadException;
import io.github.wasabithumb.jdnsbench.asset.loader.AssetLoader;
import org.jetbrains.annotations.NotNull;

import java.awt.image.BufferedImage;

import static io.github.wasabithumb.jdnsbench.util.ReflectUtil.getCodeSource;

public class UIAssets {

    private final AssetLoader loader;
    private final UIAsset<?>[] assets;
    public UIAssets() {
        this.loader = AssetLoader.fromCodeSource(getCodeSource());
        this.assets = new UIAsset<?>[] {
                /* 0 */ new UIAsset<>(UIAssetType.IMAGE, "images/icon.png")
        };
    }

    public void preload() throws AssetLoadException {
        for (UIAsset<?> asset : this.assets) {
            asset.preload(this.loader);
        }
    }

    public @NotNull BufferedImage getIcon() throws AssetLoadException {
        return this.getImage(0);
    }

    //

    private @NotNull BufferedImage getImage(int index) throws AssetLoadException {
        return this.get(UIAssetType.IMAGE, index);
    }

    private <T> T get(@NotNull UIAssetType<T> type, int index) throws AssetLoadException {
        UIAsset<?> asset = this.assets[index];
        if (!asset.type().equals(type))
            throw new AssertionError("Asset #" + index + " is not of type " + type.name() +
                    " (got " + asset.type().name() + ")");
        return type.dataClass().cast(asset.get(this.loader));
    }

}
