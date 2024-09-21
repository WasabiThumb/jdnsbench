package io.github.wasabithumb.jdnsbench.util.collections;

import org.jetbrains.annotations.NotNull;

import java.util.AbstractList;
import java.util.ConcurrentModificationException;
import java.util.List;

public class FlatteningList<T> extends AbstractList<T> {

    private final List<List<T>> backing;
    private final int count;
    public FlatteningList(List<List<T>> backing) {
        this.backing = backing;
        int size = 0;
        for (List<T> list : backing) size += list.size();
        this.count = size;
    }

    @Override
    public int size() {
        return this.count;
    }

    @Override
    public T get(int i) {
        ListIndexPair<T> pair = this.resolve(i);
        return pair.list.get(pair.index);
    }

    @Override
    public T set(int index, T element) {
        ListIndexPair<T> pair = this.resolve(index);
        return pair.list.set(pair.index, element);
    }

    private @NotNull ListIndexPair<T> resolve(int index) throws IndexOutOfBoundsException {
        if (index < 0 || index >= this.count)
            throw new IndexOutOfBoundsException("Index " + index + " out of bounds for length " + this.count);

        int size;
        for (List<T> list : this.backing) {
            size = list.size();
            if (index < size) return new ListIndexPair<>(list, index);
            index -= size;
        }

        throw new ConcurrentModificationException();
    }

    //

    private record ListIndexPair<Q>(List<Q> list, int index) { }

}
