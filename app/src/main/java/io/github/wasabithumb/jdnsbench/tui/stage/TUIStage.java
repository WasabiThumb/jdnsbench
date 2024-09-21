package io.github.wasabithumb.jdnsbench.tui.stage;

import io.github.wasabithumb.jdnsbench.tui.TUI;
import io.github.wasabithumb.jdnsbench.tui.TUIArrowKey;
import org.jetbrains.annotations.NotNull;

public interface TUIStage {

    default void onAttach(@NotNull TUI tui) { }

    default void onDetach(@NotNull TUI tui) { }

    default void draw(@NotNull TUI tui) { }

    default boolean alwaysDraw() {
        return false;
    }

    default void onInput(@NotNull TUI tui, char input) { }

    default void onInputArrowKey(@NotNull TUI tui, @NotNull TUIArrowKey key) { }

}
