package io.github.wasabithumb.jdnsbench.tui.stage.impl.config;

import io.github.wasabithumb.jdnsbench.tui.TUI;
import io.github.wasabithumb.jdnsbench.tui.TUIArrowKey;
import io.github.wasabithumb.jdnsbench.tui.bitmap.CharColor;
import io.github.wasabithumb.jdnsbench.tui.bitmap.ColoredCharCanvas;
import io.github.wasabithumb.jdnsbench.tui.stage.impl.ConfigStage;
import org.jetbrains.annotations.NotNull;

abstract class TextInputModal extends FramedModal {

    protected final String title;
    protected final StringBuilder value;
    protected int selection = 0;
    public TextInputModal(@NotNull String title, @NotNull CharSequence initialValue) {
        super();
        this.title = title;
        this.value = new StringBuilder(Math.max(initialValue.length(), this.getMaxLength()));
        this.value.append(initialValue);
    }

    public abstract int getMaxLength();

    public abstract boolean canSubmit();

    protected void onSubmit(@NotNull TUI tui, @NotNull ConfigStage stage) { }

    protected boolean isValidChar(char c) {
        if (c >= ' ' && c <= '~') return true; // ASCII
        return Character.isAlphabetic(c) || Character.isDigit(c);
    }

    protected void onCancel(@NotNull TUI tui, @NotNull ConfigStage stage) { }

    @Override
    protected int[] calculateInnerSize() {
        int innerWidth = 13; // space for OK & CANCEL buttons
        innerWidth = Math.max(innerWidth, this.getMaxLength());
        innerWidth = Math.max(innerWidth, this.title.length());
        return new int[] { innerWidth, 5 };
    }

    @Override
    protected void drawContent(@NotNull ColoredCharCanvas canvas, boolean compressed) {
        final int w = canvas.getWidth();
        final int h = canvas.getHeight();

        canvas.setForegroundColorRect(0, 0, w, 1, CharColor.MAGENTA_BRIGHT);
        if (compressed && this.title.length() > w) {
            canvas.type(0, 0, this.title.substring(0, w));
            for (int i=(w - 1); i >=0; i--) {
                canvas.setChar(i, 0, '.');
            }
        } else {
            canvas.typeCentered(0, 0, this.title, w);
        }

        canvas.setBackgroundColorRect(
                0, 2,
                w, 1,
                this.canSubmit() ? CharColor.BLACK_BRIGHT : CharColor.RED
        );
        if (compressed && this.value.length() > w) {
            int head = this.value.length() - 1;
            for (int x=(w - 1); x >= 0; x--) {
                canvas.setChar(x, 2, this.value.charAt(head--));
            }
        } else {
            canvas.typeCentered(0, 2, this.value, w);
        }

        // canvas.setRow(3, '¯');

        canvas.setBackgroundColorRect(
                0, h - 1,
                4, 1,
                this.selection == 0 ? CharColor.MAGENTA : CharColor.BLACK_BRIGHT
        );
        canvas.type(1, h - 1, "OK");

        canvas.setBackgroundColorRect(
                w - 8, h - 1,
                8, 1,
                this.selection == 1 ? CharColor.MAGENTA : CharColor.BLACK_BRIGHT
        );
        canvas.type(w - 7, h - 1, "CANCEL");
    }

    @Override
    public boolean onInput(@NotNull TUI tui, @NotNull ConfigStage stage, char input) {
        if (input == ((char) 32) || input == ((char) 13) || input == ((char) 10)) {
            if (this.selection == 0) {
                if (!this.canSubmit()) return false;
                this.onSubmit(tui, stage);
            } else {
                this.onCancel(tui, stage);
            }
            return true;
        } else if (input == ((char) 127) || input == ((char) 8)) {
            int len = this.value.length();
            if (len == 0) return false;
            this.value.setLength(len - 1);
        } else if (this.isValidChar(input) && this.value.length() < this.getMaxLength()) {
            this.value.append(input);
        }
        return false;
    }

    @Override
    public void onInputArrowKey(@NotNull TUI tui, @NotNull ConfigStage stage, @NotNull TUIArrowKey key) {
        if (key == TUIArrowKey.LEFT || key == TUIArrowKey.RIGHT) {
            this.selection = (this.selection == 0) ? 1 : 0;
        }
    }

}
