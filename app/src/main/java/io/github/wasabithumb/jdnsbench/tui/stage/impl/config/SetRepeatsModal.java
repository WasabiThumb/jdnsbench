package io.github.wasabithumb.jdnsbench.tui.stage.impl.config;

import io.github.wasabithumb.jdnsbench.tui.TUI;
import io.github.wasabithumb.jdnsbench.tui.stage.impl.ConfigStage;
import org.jetbrains.annotations.NotNull;

class SetRepeatsModal extends NumberInputModal {

    public SetRepeatsModal(int initialValue) {
        super("REPEATS", initialValue);
    }

    @Override
    public int getMaxLength() {
        return 4;
    }

    @Override
    protected void onSubmit(@NotNull TUI tui, @NotNull ConfigStage stage) {
        stage.reps = this.asInt();
    }

}
