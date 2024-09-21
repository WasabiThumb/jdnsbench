package io.github.wasabithumb.jdnsbench.api.address.provider;

import io.github.wasabithumb.jdnsbench.api.address.Address;
import io.github.wasabithumb.jdnsbench.api.address.source.AddressSource;
import org.jetbrains.annotations.NotNull;

public record SingleAddressProvider(@NotNull Address handle) implements AddressProvider {

    @Override
    public @NotNull String label() {
        String label = this.handle.label();
        return label == null ? this.handle.address() : label;
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public boolean isSource() {
        return false;
    }

    @Override
    public @NotNull AddressSource asSource() {
        throw new UnsupportedOperationException("Cannot resolve SingleAddressProvider as AddressSource");
    }

}
