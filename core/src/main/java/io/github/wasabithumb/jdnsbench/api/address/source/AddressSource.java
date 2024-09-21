package io.github.wasabithumb.jdnsbench.api.address.source;

import io.github.wasabithumb.jdnsbench.api.address.Address;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Collection;

public interface AddressSource {

    @NotNull String label();

    @NotNull Collection<Address> get() throws IOException;

}
