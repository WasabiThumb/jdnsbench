package io.github.wasabithumb.jdnsbench.tui.stage.impl.config;

import io.github.wasabithumb.jdnsbench.tui.TUI;
import io.github.wasabithumb.jdnsbench.tui.stage.impl.ConfigStage;
import org.jetbrains.annotations.NotNull;

class SetDomainModal extends TextInputModal {

    public SetDomainModal(@NotNull CharSequence initialValue) {
        super("DOMAIN", initialValue);
    }

    @Override
    public int getMaxLength() {
        return 64;
    }

    @Override
    public boolean canSubmit() {
        return !this.value.isEmpty();
    }

    @Override
    protected boolean isValidChar(char c) {
        return ('a' <= c && c <= 'z') || ('A' <= c && c <= 'Z') || ('0' <= c && c <= '9') || c == '.' || c == '-';
    }

    @Override
    protected void onSubmit(@NotNull TUI tui, @NotNull ConfigStage stage) {
        stage.domain = this.value.toString();
    }

}
