package io.github.wasabithumb.jdnsbench.tui.bitmap;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class CharCanvas implements CharBitmap {

    private static final long INITIALIZER = 0x0020002000200020L;
    protected static final int FLAG_FULL_WIDTH = 1;
    protected static final int FLAG_FULL_HEIGHT = 2;

    protected final long[] data;
    protected final int rs;
    protected final int x;
    protected final int y;
    protected final int w;
    protected final int h;
    protected final int flags;
    protected CharCanvas(long[] data, int rs, int x, int y, int w, int h, int flags) {
        this.data = data;
        this.rs = rs;
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.flags = flags;
    }

    public CharCanvas(int width, int height) {
        final int rs = calcRowSize(width);
        assertValidHeight(height);

        this.data = new long[rs * height];
        this.rs = rs;
        this.x = 0;
        this.y = 0;
        this.w = width;
        this.h = height;
        this.flags = FLAG_FULL_WIDTH | FLAG_FULL_HEIGHT;

        Arrays.fill(this.data, INITIALIZER);
    }

    //

    public @NotNull CharCanvas subCanvas(int x, int y, int w, int h) {
        this.boundsCheck(x, y);
        this.boundsCheck(x + assertValidWidth(w) - 1, y + assertValidHeight(h) - 1);

        int flags = 0;
        if ((this.flags & FLAG_FULL_WIDTH) != 0 && this.w == w) {
            flags |= FLAG_FULL_WIDTH;
        }
        if ((this.flags & FLAG_FULL_HEIGHT) != 0 && this.h == h) {
            flags |= FLAG_FULL_HEIGHT;
        }

        return this.subCanvasInternal(this.x + x, this.y + y, w, h, flags);
    }

    @Contract("_, _, _, _, _ -> new")
    protected @NotNull CharCanvas subCanvasInternal(int x, int y, int w, int h, int flags) {
        return new CharCanvas(this.data, this.rs, x, y, w, h, flags);
    }

    @Override
    public int getWidth() {
        return this.w;
    }

    @Override
    public int getHeight() {
        return this.h;
    }

    @Override
    public char getChar(int x, int y) throws IndexOutOfBoundsException {
        this.boundsCheck(x, y);
        x += this.x;
        y += this.y;

        int index = (y * (this.rs << 2)) + x;
        int octet = index << 1;
        int element = octet >> 3;
        octet &= 7;

        long bits = this.data[element];
        int value = (int) ((bits >> (octet << 3)) & 0xFFFFL);

        return (char) value;
    }

    @Override
    public void setChar(int x, int y, char value) throws IndexOutOfBoundsException {
        this.boundsCheck(x, y);
        x += this.x;
        y += this.y;

        int index = (y * (this.rs << 2)) + x;
        int octet = index << 1;
        int element = octet >> 3;
        octet &= 7;

        int shl = (octet << 3);
        long mask = ~(0xFFFFL << shl);
        long data = ((long) value) << shl;

        long bits = this.data[element];
        bits &= mask;
        bits |= data;
        this.data[element] = bits;
    }

    @Override
    public void setRow(int y, char c) {
        if ((this.flags & FLAG_FULL_WIDTH) != 0) {
            this.boundsCheckY(y);
            y += this.y;

            final long lv = this.c4(c);
            final int start = y * this.rs;
            final int end = start + this.rs;

            for (int i = start; i < end; i++) this.data[i] = lv;
        } else {
            CharBitmap.super.setRow(y, c);
        }
    }

    @Override
    public void setColumn(int x, char c) {
        this.boundsCheckX(x);
        x += this.x;

        int element = x >> 2;
        if (this.y != 0) element += (this.rs * this.y);
        int shl = (((x << 1) & 7) << 3);
        long mask = ~(0xFFFFL << shl);
        long data = ((long) c) << shl;

        for (int y=0; y < this.h; y++) {
            this.data[element] = (this.data[element] & mask) | data;
            element += this.rs;
        }
    }

    @Override
    public void fill(char c) {
        if ((this.flags & FLAG_FULL_WIDTH) != 0 && (this.flags & FLAG_FULL_HEIGHT) != 0) {
            Arrays.fill(this.data, this.c4(c));
        } else {
            CharBitmap.super.fill(c);
        }
    }

    public void print(boolean trailingNewline) {
        for (int y=0; y < this.h; y++) {
            System.out.print(this.getRow(y));
            if (trailingNewline || (y != (this.h - 1))) System.out.print('\n');
        }
    }

    public void print() {
        this.print(false);
    }

    protected void boundsCheck(int x, int y) throws IndexOutOfBoundsException {
        this.boundsCheckX(x);
        this.boundsCheckY(y);
    }

    protected void boundsCheckX(int x) throws IndexOutOfBoundsException {
        if (x < 0 || x >= this.w)
            throw new IndexOutOfBoundsException("X index " + x + " out of bounds for width " + this.w);
    }

    protected void boundsCheckY(int y) throws IndexOutOfBoundsException {
        if (y < 0 || y >= this.h)
            throw new IndexOutOfBoundsException("Y index " + y + " out of bounds for height " + this.h);
    }

    protected long c4(char c) {
        //noinspection RedundantCast
        int iv = (int) c;
        iv = (iv << 16) | iv;

        //noinspection RedundantCast
        long lv = (long) iv;
        lv = (lv << 32) | lv;

        return lv;
    }

    //

    private static int calcRowSize(int width) throws IllegalArgumentException {
        return ((assertValidWidth(width) - 1) >> 2) + 1;
    }

    protected static int assertValidWidth(int width) throws IllegalArgumentException {
        assertValidDimension(width, "Width");
        return width;
    }

    protected static int assertValidHeight(int height) throws IllegalArgumentException {
        assertValidDimension(height, "Height");
        return height;
    }

    private static void assertValidDimension(int dimension, String name) throws IllegalArgumentException {
        if (dimension < 1) throw new IllegalArgumentException(name + " must be at least 1");
    }
}
