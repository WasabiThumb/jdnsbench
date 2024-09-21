package io.github.wasabithumb.jdnsbench.tui.bitmap;

import org.jetbrains.annotations.NotNull;

public interface CharBitmap {

    int getWidth();

    int getHeight();

    char getChar(int x, int y) throws IndexOutOfBoundsException;

    default void setChar(int x, int y, char c) throws IndexOutOfBoundsException, UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    default void type(int x, int y, @NotNull CharSequence text) {
        for (int i = 0; i < text.length(); i++) {
            setChar(x + i, y, text.charAt(i));
        }
    }

    default int typeCentered(int x, int y, @NotNull CharSequence text, int w) {
        x += Math.floorDiv(w - text.length(), 2);
        this.type(x, y, text);
        return x;
    }

    default @NotNull String getRow(int y) {
        final int w = this.getWidth();
        char[] row = new char[w];
        for (int x = 0; x < w; x++) {
            row[x] = this.getChar(x, y);
        }
        return new String(row);
    }

    default void print(boolean trailingNewline) {
        final int h = this.getHeight();
        for (int y=0; y < h; y++) {
            System.out.print(this.getRow(y));
            if (trailingNewline || (y != (h - 1))) System.out.print('\n');
        }
    }

    default void print() {
        this.print(false);
    }

    default void setRow(int y, char c) {
        for (int x = 0; x < this.getWidth(); x++) {
            this.setChar(x, y, c);
        }
    }

    default void setColumn(int x, char c) {
        for (int y = 0; y < this.getHeight(); y++) {
            this.setChar(x, y, c);
        }
    }

    default void fill(char c) {
        for (int y=0; y < this.getHeight(); y++) {
            this.setRow(y, c);
        }
    }

}
