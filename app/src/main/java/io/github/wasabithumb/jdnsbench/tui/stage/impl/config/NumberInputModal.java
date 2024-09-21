package io.github.wasabithumb.jdnsbench.tui.stage.impl.config;

import org.jetbrains.annotations.NotNull;

abstract class NumberInputModal extends TextInputModal {

    public NumberInputModal(@NotNull String title, @NotNull Number initialValue) {
        super(title, initialValue.toString());
    }

    public int asInt() {
        int ret = 0;
        char c;
        for (int i=0; i < this.value.length(); i++) {
            ret *= 10;
            c = this.value.charAt(i);
            ret += (c - '0');
        }
        return ret;
    }

    public long asLong() {
        return Long.parseLong(this.value.toString());
    }

    @Override
    public boolean canSubmit() {
        return (!this.value.isEmpty()) && this.value.charAt(0) != '0';
    }

    @Override
    protected boolean isValidChar(char c) {
        return '0' <= c && c <= '9';
    }

}
