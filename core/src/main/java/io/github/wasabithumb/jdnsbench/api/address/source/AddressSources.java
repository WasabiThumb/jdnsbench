package io.github.wasabithumb.jdnsbench.api.address.source;

import io.github.wasabithumb.jdnsbench.asset.CoreResources;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public final class AddressSources {

    private static final AddressSource DEFAULTS = new AssetCSVAddressSource(
            CoreResources.getLoader(),
            "Default Servers",
            "data/default_servers.csv"
    );

    private static final AddressSource ALL = new HttpCSVAddressSource(
            "public-dns.info : All Servers",
            "https://public-dns.info/nameservers.csv"
    );

    @Contract(pure = true)
    public static @NotNull AddressSource all() {
        return ALL;
    }

    @Contract(pure = true)
    public static @NotNull AddressSource defaults() {
        return DEFAULTS;
    }

    public static @NotNull AddressSource country(@NotNull String countryCode) {
        return country(countryCode, countryCode.toUpperCase(Locale.ROOT));
    }

    public static @NotNull AddressSource country(@NotNull String countryCode, @NotNull String countryName) {
        return new HttpCSVAddressSource(
                "public-dns.info : " + countryName,
                "https://public-dns.info/nameserver/" + countryCode.toLowerCase(Locale.ROOT) + ".csv"
        );
    }

}
