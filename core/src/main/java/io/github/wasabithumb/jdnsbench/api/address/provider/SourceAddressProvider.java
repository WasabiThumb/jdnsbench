package io.github.wasabithumb.jdnsbench.api.address.provider;

import io.github.wasabithumb.jdnsbench.api.address.Address;
import io.github.wasabithumb.jdnsbench.api.address.source.AddressSource;
import org.jetbrains.annotations.NotNull;

public record SourceAddressProvider(@NotNull AddressSource handle) implements AddressProvider {

    @Override
    public @NotNull String label() {
        return this.handle.label();
    }

    @Override
    public boolean isSingle() {
        return false;
    }

    @Override
    public boolean isSource() {
        return true;
    }

    @Override
    public @NotNull Address asSingle() {
        throw new UnsupportedOperationException("Cannot resolve SourceAddressProvider as Address");
    }

}
