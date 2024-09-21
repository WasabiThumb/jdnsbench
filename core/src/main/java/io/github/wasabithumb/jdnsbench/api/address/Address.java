package io.github.wasabithumb.jdnsbench.api.address;

import static io.github.wasabithumb.jdnsbench.util.IPUtil.*;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class Address implements Comparable<Address> {

    public static @NotNull Address of(@NotNull CharSequence address) throws IllegalArgumentException {
        if (isValidV4(address)) return new Address(String.valueOf(address), AddressType.V4, null, true);
        if (isValidV6(address)) return new Address(String.valueOf(address), AddressType.V6, null, true);
        throw new IllegalArgumentException("Invalid IP address: " + address);
    }

    public static @NotNull Address v4(@NotNull String address) {
        return new Address(address, AddressType.V4, null, false);
    }

    public static @NotNull Address v6(@NotNull String address) {
        return new Address(address, AddressType.V6, null, false);
    }

    //

    private final String address;
    private final AddressType type;
    private final String label;
    private boolean validated;
    Address(String address, AddressType type, String label, boolean validated) {
        this.address = address;
        this.type = type;
        this.label = label;
        this.validated = validated;
    }

    public @NotNull String address() {
        return this.address;
    }

    public @NotNull AddressType type() {
        return this.type;
    }

    public @Nullable String label() {
        return this.label;
    }

    @Contract("_ -> new")
    public @NotNull Address label(@Nullable String label) {
        return new Address(this.address, this.type, label, this.validated);
    }

    @Contract(" -> this")
    public @NotNull Address validate() throws AssertionError {
        synchronized (this) {
            if (this.validated) return this;
            switch (this.type) {
                case V4 -> {
                    if (!isValidV4(this.address))
                        throw new AssertionError("Invalid IPv4 address: " + this.address);
                }
                case V6 -> {
                    if (!isValidV6(this.address))
                        throw new AssertionError("Invalid IPv6 address: " + this.address);
                }
            }
            this.validated = true;
        }
        return this;
    }

    //

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + this.address.hashCode();
        hash = 31 * hash + this.type.hashCode();
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj instanceof Address other) {
            if (this.address.equals(other.address()) && this.type.equals(other.type()))
                return true;
        }
        return super.equals(obj);
    }

    @Contract(pure = true)
    @Override
    public @NotNull String toString() {
        return "JDNSAddress[address=" + this.address + ", type=" + this.type.name() + ", label=" + this.label + "]";
    }

    @Override
    public int compareTo(@NotNull Address other) {
        final String a = this.label == null ? this.address : this.label;
        final String b = other.label == null ? other.address : other.label;
        return a.compareTo(b);
    }

}
