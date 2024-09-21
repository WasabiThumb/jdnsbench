package io.github.wasabithumb.jdnsbench.util.collections;

import java.util.AbstractList;
import java.util.List;

public class FlipList<T> extends AbstractList<T> {

    private final List<T> backing;
    public FlipList(List<T> backing) {
        this.backing = backing;
    }

    private int remap(int index) {
        return this.backing.size() - 1 - index;
    }

    @Override
    public int size() {
        return this.backing.size();
    }

    @Override
    public T get(int i) {
        return this.backing.get(this.remap(i));
    }

    @Override
    public T set(int index, T element) {
        return this.backing.set(this.remap(index), element);
    }

    @Override
    public T remove(int index) {
        return this.backing.remove(this.remap(index));
    }

    @Override
    public void clear() {
        this.backing.clear();
    }

    @Override
    public boolean contains(Object o) {
        return this.backing.contains(o);
    }

    @Override
    public void add(int index, T element) {
        // Can't use #remap(int) due to #add(int,T) implementation details
        this.backing.add(this.backing.size() - index, element);
    }

}
