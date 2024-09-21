package io.github.wasabithumb.jdnsbench.api.bench;

import io.github.wasabithumb.jdnsbench.api.address.Address;
import io.github.wasabithumb.jdnsbench.api.address.source.AddressSource;
import io.github.wasabithumb.jdnsbench.api.address.source.AddressSources;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @param domain Domain to resolve for testing nameservers
 * @param nameservers Nameservers to benchmark
 * @param nameserverSources Sources to fetch nameserver lists from; unioned with "nameservers"
 * @param timeout Maximum timeout in milliseconds, the system timeout may be shorter than this
 */
public record JDNSBenchOptions(
        @NotNull String domain,
        @NotNull Collection<Address> nameservers,
        @NotNull Collection<AddressSource> nameserverSources,
        long timeout,
        boolean useV6,
        int reps,
        long period
) {

    public static JDNSBenchOptions DEFAULT = builder()
            .nameserverSource(AddressSources.defaults())
            .build();

    @Contract(" -> new")
    public static @NotNull Builder builder() {
        return new Builder();
    }

    //

    public static class Builder {

        private String domain = "github.io";
        private Collection<Address> nameservers = Collections.emptyList();
        private int nameserversState = 0;
        private Collection<AddressSource> nameserverSources = Collections.emptyList();
        private int nameserverSourcesState = 0;
        private long timeout = 1000L;
        private boolean useV6 = false;
        private int reps = 10;
        private long period = 50L;

        @Contract("_ -> this")
        public @NotNull Builder domain(final @NotNull String domain) {
            this.domain = domain;
            return this;
        }

        @Contract(pure = true)
        public @NotNull String domain() {
            return this.domain;
        }

        @Contract("_ -> this")
        public @NotNull Builder nameservers(final @NotNull Collection<Address> nameservers) {
            switch (this.nameserversState) {
                case 0:
                    this.nameservers = nameservers;
                    this.nameserversState = 1;
                    break;
                case 1:
                    List<Address> newList = new ArrayList<>(this.nameservers.size() + nameservers.size());
                    newList.addAll(this.nameservers);
                    this.nameservers = newList;
                    this.nameserversState = 2;
                case 2:
                    this.nameservers.addAll(nameservers);
                    break;
            }
            return this;
        }

        @Contract("_ -> this")
        public @NotNull Builder nameserver(final @NotNull Address nameserver) {
            return this.nameservers(Collections.singletonList(nameserver));
        }

        public @NotNull Collection<Address> nameservers() {
            Collection<Address> nameservers = this.nameservers;
            if (this.nameserversState == 0) return nameservers;
            if (nameservers instanceof List<Address> list) {
                return Collections.unmodifiableList(list);
            }
            return Collections.unmodifiableCollection(nameservers);
        }

        @Contract("_ -> this")
        public @NotNull Builder nameserverSources(final @NotNull Collection<AddressSource> nameserverSources) {
            switch (this.nameserverSourcesState) {
                case 0:
                    this.nameserverSources = nameserverSources;
                    this.nameserverSourcesState = 1;
                    break;
                case 1:
                    List<AddressSource> newList = new ArrayList<>(this.nameserverSources.size() + nameserverSources.size());
                    newList.addAll(this.nameserverSources);
                    this.nameserverSources = newList;
                    this.nameserverSourcesState = 2;
                case 2:
                    this.nameserverSources.addAll(nameserverSources);
                    break;
            }
            return this;
        }

        @Contract("_ -> this")
        public @NotNull Builder nameserverSource(final @NotNull AddressSource nameserverSource) {
            return this.nameserverSources(Collections.singletonList(nameserverSource));
        }

        public @NotNull Collection<AddressSource> nameserverSources() {
            return Collections.unmodifiableCollection(this.nameserverSources);
        }

        @Contract("_ -> this")
        public @NotNull Builder timeout(final long timeout) {
            this.timeout = timeout;
            return this;
        }

        public long timeout() {
            return this.timeout;
        }

        @Contract("_ -> this")
        public @NotNull Builder useV6(final boolean useV6) {
            this.useV6 = useV6;
            return this;
        }

        public boolean useV6() {
            return this.useV6;
        }

        @Contract("_ -> this")
        public @NotNull Builder reps(final int reps) {
            this.reps = reps;
            return this;
        }

        public int reps() {
            return this.reps;
        }

        @Contract("_ -> this")
        public @NotNull Builder period(final long period) {
            this.period = period;
            return this;
        }

        public long period() {
            return this.period;
        }

        @Contract(" -> new")
        public @NotNull JDNSBenchOptions build() {
            return new JDNSBenchOptions(
                    this.domain,
                    this.nameservers(),
                    this.nameserverSources(),
                    this.timeout,
                    this.useV6,
                    this.reps,
                    this.period
            );
        }

    }

}
