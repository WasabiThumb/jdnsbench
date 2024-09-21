package io.github.wasabithumb.jdnsbench.util;

import org.jetbrains.annotations.NotNull;

import java.util.PrimitiveIterator;
import java.util.stream.IntStream;

public class JoinedCharSequence implements CharSequence {

    private final CharSequence a;
    private final CharSequence b;
    private final int off;
    private final int len;
    private final boolean full;
    JoinedCharSequence(@NotNull CharSequence a, @NotNull CharSequence b, int off, int len, boolean full) {
        this.a = a;
        this.b = b;
        this.off = off;
        this.len = len;
        this.full = full;
    }

    public JoinedCharSequence(@NotNull CharSequence a, @NotNull CharSequence b) {
        this(a, b, 0, a.length() + b.length(), true);
    }

    private void boundsCheck(int i) {
        if (i < 0 || i >= this.len)
            throw new IndexOutOfBoundsException("Index " + i + " out of bounds for length " + this.len);
    }

    @Override
    public int length() {
        return this.len;
    }

    @Override
    public char charAt(int i) {
        this.boundsCheck(i);
        i += this.off;
        final int al = this.a.length();
        if (i >= al) {
            return this.b.charAt(i - al);
        } else {
            return this.a.charAt(i);
        }
    }

    @Override
    public boolean isEmpty() {
        return this.a.isEmpty() && this.b.isEmpty();
    }

    @Override
    public @NotNull CharSequence subSequence(int from, int to) {
        final int absFrom = from + this.off;
        final int absTo = to + this.off;
        final int al = this.a.length();
        boolean fromB = (absFrom >= al);
        boolean toB = (absTo >= al);
        if (fromB == toB) {
            if (fromB) {
                return this.b.subSequence(absFrom - al, absTo - al);
            } else {
                return this.a.subSequence(absFrom, to);
            }
        }
        this.boundsCheck(from);
        if (to < from)
            throw new IndexOutOfBoundsException("Upper bound " + to + " is smaller than lower bound " + from);
        this.boundsCheck(to - 1);
        return new JoinedCharSequence(
                this.a,
                this.b,
                from,
                to - from,
                this.full && from == 0 && to == this.len
        );
    }

    @Override
    public @NotNull IntStream chars() {
        if (this.full) return IntStream.concat(this.a.chars(), this.b.chars());
        return CharSequence.super.chars();
    }

    @Override
    public @NotNull IntStream codePoints() {
        if (this.full) return IntStream.concat(this.a.codePoints(), this.b.codePoints());
        return CharSequence.super.codePoints();
    }

    @Override
    public int hashCode() {
        final PrimitiveIterator.OfInt chars = this.chars().iterator();
        int hash = 0;
        while (chars.hasNext()) {
            hash = 31 * hash + chars.nextInt();
        }
        return hash;
    }

    @Override
    public @NotNull String toString() {
        char[] buf = new char[this.len];
        final PrimitiveIterator.OfInt chars = this.chars().iterator();
        for (int i = 0; i < this.len; i++) buf[i] = (char) chars.nextInt();
        return new String(buf);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj instanceof CharSequence other) {
            return CharSequence.compare(this, other) == 0;
        }
        return super.equals(obj);
    }

}
