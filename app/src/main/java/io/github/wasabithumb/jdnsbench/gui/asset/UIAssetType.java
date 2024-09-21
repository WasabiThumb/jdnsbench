package io.github.wasabithumb.jdnsbench.gui.asset;

import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

sealed interface UIAssetType<T> {

    UIAssetType<BufferedImage> IMAGE = new Image();

    //

    @NotNull String name();

    @NotNull Class<T> dataClass();

    @NotNull T readFromStream(@NotNull InputStream stream) throws IOException;

    default @NotNull T readFromBytes(byte @NotNull [] bytes) throws IOException {
        return this.readFromStream(new BufferedInputStream(new ByteArrayInputStream(bytes)));
    }

    //

    final class Image implements UIAssetType<BufferedImage> {

        @Override
        public @NotNull String name() {
            return "IMAGE";
        }

        @Override
        public @NotNull Class<BufferedImage> dataClass() {
            return BufferedImage.class;
        }

        @Override
        public @NotNull BufferedImage readFromStream(@NotNull InputStream stream) throws IOException {
            return ImageIO.read(stream);
        }

    }

}
