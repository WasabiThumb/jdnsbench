package io.github.wasabithumb.jdnsbench.util.collections;

import org.jetbrains.annotations.NotNull;

import java.util.AbstractList;
import java.util.Objects;

public class FixedIntArrayList extends AbstractList<Integer> {

    private final int[] backing;
    public FixedIntArrayList(final int[] backing) {
        this.backing = backing;
    }

    @Override
    public int size() {
        return this.backing.length;
    }

    @Override
    public @NotNull Integer get(int i) {
        return this.backing[i];
    }

    @Override
    public @NotNull Integer set(int index, @NotNull Integer element) {
        final Integer ret = this.backing[index];
        this.backing[index] = Objects.requireNonNull(element);
        return ret;
    }

}
