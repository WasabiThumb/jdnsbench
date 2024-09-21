package io.github.wasabithumb.jdnsbench.tui.stage.impl.config;

import io.github.wasabithumb.jdnsbench.tui.TUI;
import io.github.wasabithumb.jdnsbench.tui.stage.impl.ConfigStage;
import org.jetbrains.annotations.NotNull;

class SetDelayModal extends NumberInputModal {

    public SetDelayModal(long initialValue) {
        super("DELAY (MS)", initialValue);
    }

    @Override
    public int getMaxLength() {
        return 6;
    }

    @Override
    protected void onSubmit(@NotNull TUI tui, @NotNull ConfigStage stage) {
        stage.period = this.asLong();
    }

}
