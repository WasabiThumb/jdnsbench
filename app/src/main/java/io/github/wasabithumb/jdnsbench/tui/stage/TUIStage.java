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

    default void tooSmall(@NotNull TUI tui) {
        System.out.print(tui.ansi().eraseScreen().cursor(0, 0));
        System.out.println(tui.ansi().fgBrightRed().a("Your terminal is too small!"));
        System.out.println(tui.ansi().fgBrightRed().a("Resize your terminal or press ESC to exit."));
    }

}
