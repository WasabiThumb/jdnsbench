package io.github.wasabithumb.jdnsbench.api.address.provider;

import io.github.wasabithumb.jdnsbench.api.address.Address;
import io.github.wasabithumb.jdnsbench.api.address.source.AddressSource;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.NonExtendable
public interface AddressProvider {

    static @NotNull SingleAddressProvider of(@NotNull Address address) {
        return new SingleAddressProvider(address);
    }

    static @NotNull SourceAddressProvider of(@NotNull AddressSource source) {
        return new SourceAddressProvider(source);
    }

    //

    @NotNull String label();

    @NotNull Object handle();

    default boolean isSingle() {
        return (this.handle() instanceof Address);
    }

    default @NotNull Address asSingle() {
        return (Address) this.handle();
    }

    default boolean isSource() {
        return (this.handle() instanceof AddressSource);
    }

    default @NotNull AddressSource asSource() {
        return (AddressSource) this.handle();
    }

}
