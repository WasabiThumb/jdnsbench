package io.github.wasabithumb.jdnsbench.tui;

import org.jetbrains.annotations.NotNull;

public enum TUIArrowKey {
    INVALID,
    UP,
    DOWN,
    LEFT,
    RIGHT;

    public static @NotNull TUIArrowKey of(char c) {
        return switch (c) {
            case 'A' -> UP;
            case 'B' -> DOWN;
            case 'C' -> RIGHT;
            case 'D' -> LEFT;
            default -> INVALID;
        };
    }
}
