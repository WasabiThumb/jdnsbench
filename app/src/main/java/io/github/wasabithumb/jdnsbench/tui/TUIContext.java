package io.github.wasabithumb.jdnsbench.tui;

import io.github.wasabithumb.jdnsbench.api.bench.JDNSBenchOptions;
import org.jetbrains.annotations.NotNull;

public class TUIContext {

    // OPTIONS
    private JDNSBenchOptions options = JDNSBenchOptions.DEFAULT;
    public @NotNull JDNSBenchOptions options() {
        return this.options;
    }
    public void options(@NotNull JDNSBenchOptions options) {
        this.options = options;
    }

}
