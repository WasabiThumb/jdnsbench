package io.github.wasabithumb.jdnsbench.util.collections;

import org.jetbrains.annotations.NotNull;

import java.util.AbstractList;
import java.util.List;
import java.util.function.Function;

public class MappingList<A, B> extends AbstractList<B> {

    private final List<A> backing;
    private final Function<A, B> mappingFunction;
    public MappingList(@NotNull List<A> backing, @NotNull Function<A, B> mappingFunction) {
        this.backing = backing;
        this.mappingFunction = mappingFunction;
    }

    public MappingList(@NotNull List<A> backing, final @NotNull Class<B> mappedClass) {
        this(backing, mappedClass::cast);
    }

    @Override
    public int size() {
        return this.backing.size();
    }

    @Override
    public B get(int i) {
        return this.mappingFunction.apply(this.backing.get(i));
    }

}
