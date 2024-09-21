package io.github.wasabithumb.jdnsbench.api;

import io.github.wasabithumb.jdnsbench.api.address.Address;

import java.util.List;

public record JDNSResult(long elapsed, List<Address> addresses) {

    public long elapsedMillis() {
        return Math.round(this.elapsed / 1e6d);
    }

}
