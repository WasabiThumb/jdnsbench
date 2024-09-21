package io.github.wasabithumb.jdnsbench.tui.bitmap;

import org.jetbrains.annotations.NotNull;
import org.jline.jansi.Ansi;

public class ColoredCharCanvas extends CharCanvas implements ColoredCharBitmap {

    private final byte[] colors;
    private final int cw;

    protected ColoredCharCanvas(long[] data, int rs, int x, int y, int w, int h, int flags, byte[] colors, int cw) {
        super(data, rs, x, y, w, h, flags);
        this.colors = colors;
        this.cw = cw;
    }

    public ColoredCharCanvas(int width, int height) {
        super(width, height);
        this.colors = new byte[width * height];
        this.cw = width;
    }

    //

    @Override
    public @NotNull ColoredCharCanvas subCanvas(int x, int y, int w, int h) {
        return (ColoredCharCanvas) super.subCanvas(x, y, w, h);
    }

    @Override
    protected @NotNull ColoredCharCanvas subCanvasInternal(int x, int y, int w, int h, int flags) {
        return new ColoredCharCanvas(this.data, this.rs, x, y, w, h, flags, this.colors, this.cw);
    }

    private @NotNull CharColor getColor(int x, int y, boolean hi) {
        this.boundsCheck(x, y);
        x += this.x;
        y += this.y;

        int data = Byte.toUnsignedInt(this.colors[(y * this.cw) + x]);
        if (hi) {
            data >>= 4;
        } else {
            data &= 0xF;
        }

        boolean bright = false;
        if ((data & 8) == 8) {
            data &= 7;
            bright = true;
        }

        Ansi.Color ansi = switch (data) {
            case 0 -> Ansi.Color.DEFAULT;
            case 1 -> Ansi.Color.BLACK;
            case 2 -> Ansi.Color.RED;
            case 3 -> Ansi.Color.GREEN;
            case 4 -> Ansi.Color.YELLOW;
            case 5 -> Ansi.Color.BLUE;
            case 6 -> Ansi.Color.MAGENTA;
            case 7 -> Ansi.Color.CYAN;
            default -> throw new AssertionError("Illegal value: " + data);
        };
        return new CharColor(ansi, bright);
    }

    private void setColor(int x, int y, boolean hi, @NotNull CharColor color) {
        this.boundsCheck(x, y);
        x += this.x;
        y += this.y;
        final int index = (y * this.cw) + x;

        int nibble;
        if (color.color() == Ansi.Color.DEFAULT || color.color() == Ansi.Color.WHITE) {
            nibble = 0;
        } else {
            nibble = color.color().value() + 1;
        }
        if (color.bright()) nibble |= 8;

        int data = Byte.toUnsignedInt(this.colors[index]);
        if (hi) {
            data = (data & 0x0F) | (nibble << 4);
        } else {
            data = (data & 0xF0) | nibble;
        }
        this.colors[index] = (byte) data;
    }

    @Override
    public @NotNull CharColor getForegroundColor(int x, int y) throws IndexOutOfBoundsException {
        return this.getColor(x, y, false);
    }

    @Override
    public @NotNull CharColor getBackgroundColor(int x, int y) throws IndexOutOfBoundsException {
        return this.getColor(x, y, true);
    }

    @Override
    public void setForegroundColor(int x, int y, @NotNull CharColor color) throws IndexOutOfBoundsException {
        this.setColor(x, y, false, color);
    }

    @Override
    public void setBackgroundColor(int x, int y, @NotNull CharColor color) throws IndexOutOfBoundsException {
        this.setColor(x, y, true, color);
    }

}
