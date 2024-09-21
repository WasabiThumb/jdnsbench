package io.github.wasabithumb.jdnsbench.api.bench;

import org.jetbrains.annotations.NotNull;

public enum JDNSBenchJobSort {
    STATE(Category.STATE),
    STATE_REVERSE(Category.STATE, true),
    TIME(Category.TIME),
    TIME_REVERSE(Category.TIME, true),
    LABEL(Category.LABEL),
    LABEL_REVERSE(Category.LABEL, true);

    private final Category category;
    private final boolean reversed;
    JDNSBenchJobSort(Category category, boolean reversed) {
        this.category = category;
        this.reversed = reversed;
    }

    JDNSBenchJobSort(Category category) {
        this(category, false);
    }

    public final Category getCategory() {
        return this.category;
    }

    public final boolean isReversed() {
        return this.reversed;
    }

    public @NotNull JDNSBenchJobSort reversed() {
        return switch (this.category) {
            case STATE -> this.reversed ? STATE : STATE_REVERSE;
            case TIME -> this.reversed ? TIME : TIME_REVERSE;
            case LABEL -> this.reversed ? LABEL : LABEL_REVERSE;
        };
    }

    //

    public enum Category {
        STATE,
        TIME,
        LABEL
    }
}
