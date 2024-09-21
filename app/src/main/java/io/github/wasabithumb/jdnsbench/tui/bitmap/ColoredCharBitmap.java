package io.github.wasabithumb.jdnsbench.tui.bitmap;

import org.jetbrains.annotations.NotNull;
import org.jline.jansi.Ansi;

public interface ColoredCharBitmap extends CharBitmap {

    @NotNull CharColor getForegroundColor(int x, int y) throws IndexOutOfBoundsException;

    @NotNull CharColor getBackgroundColor(int x, int y) throws IndexOutOfBoundsException;

    default void setForegroundColor(
            int x,
            int y,
            @NotNull CharColor color
    ) throws IndexOutOfBoundsException, UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    default void setForegroundColorRect(
            int x,
            int y,
            int w,
            int h,
            @NotNull CharColor color
    ) throws IndexOutOfBoundsException, UnsupportedOperationException {
        for (int dx=0; dx < w; dx++) {
            for (int dy=0; dy < h; dy++) {
                this.setForegroundColor(x + dx, y + dy, color);
            }
        }
    }

    default void setBackgroundColor(
            int x,
            int y,
            @NotNull CharColor color
    ) throws IndexOutOfBoundsException, UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    default void setBackgroundColorRect(
            int x,
            int y,
            int w,
            int h,
            @NotNull CharColor color
    ) throws IndexOutOfBoundsException, UnsupportedOperationException {
        for (int dx=0; dx < w; dx++) {
            for (int dy=0; dy < h; dy++) {
                this.setBackgroundColor(x + dx, y + dy, color);
            }
        }
    }

    @Override
    default @NotNull String getRow(int y) {
        StringBuilder sb = new StringBuilder(this.getWidth());

        CharColor bg = CharColor.DEFAULT;
        CharColor nbg;
        CharColor fg = CharColor.DEFAULT;
        CharColor nfg;

        for (int x=0; x < this.getWidth(); x++) {
            nbg = this.getBackgroundColor(x, y);
            if (!bg.isSimilar(nbg)) {
                if (nbg.bright()) {
                    sb.append(Ansi.ansi().bgBright(nbg.color()).toString());
                } else {
                    sb.append(Ansi.ansi().bg(nbg.color()).toString());
                }
                bg = nbg;
            }
            nfg = this.getForegroundColor(x, y);
            if (!fg.isSimilar(nfg)) {
                if (nfg.bright()) {
                    sb.append(Ansi.ansi().fgBright(nfg.color()).toString());
                } else {
                    sb.append(Ansi.ansi().fg(nfg.color()).toString());
                }
                fg = nfg;
            }
            sb.append(this.getChar(x, y));
        }

        if (!bg.isDefault()) sb.append(Ansi.ansi().bg(Ansi.Color.DEFAULT).toString());
        if (!fg.isDefault()) sb.append(Ansi.ansi().fg(Ansi.Color.DEFAULT).toString());

        return sb.toString();
    }

}
