package io.github.wasabithumb.jdnsbench.tui.stage.impl.config;

import io.github.wasabithumb.jdnsbench.tui.TUI;
import io.github.wasabithumb.jdnsbench.tui.TUIArrowKey;
import io.github.wasabithumb.jdnsbench.tui.bitmap.CharColor;
import io.github.wasabithumb.jdnsbench.tui.bitmap.ColoredCharCanvas;
import io.github.wasabithumb.jdnsbench.tui.stage.impl.ConfigStage;
import io.github.wasabithumb.jdnsbench.util.collections.FlatteningList;
import io.github.wasabithumb.jdnsbench.util.collections.MappingList;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

abstract class DropdownModal<T> extends FramedModal {

    protected int selection = 0;
    private int maxLabelLengthCache = -1;

    //

    protected abstract @NotNull CharSequence getTitle();

    protected abstract @NotNull List<T> getItems();

    protected void onSubmit(@NotNull TUI tui, @NotNull ConfigStage stage, @NotNull T selection) { }

    protected void onCancel(@NotNull TUI tui, @NotNull ConfigStage stage) { }

    protected int getItemCount() {
        return this.getItems().size();
    }

    protected @NotNull CharSequence getLabel(T item) {
        return Objects.toString(item);
    }

    protected final @NotNull List<CharSequence> getLabels() {
        List<CharSequence> ret = Collections.singletonList("* CANCEL *");
        ret = new FlatteningList<>(
                List.of(
                        ret,
                        new MappingList<>(this.getItems(), this::getLabel)
                )
        );
        return ret;
    }

    protected int getMaxLabelLength() {
        if (this.maxLabelLengthCache != -1) return this.maxLabelLengthCache;
        int max = 0;
        for (T item : this.getItems()) max = Math.max(max, this.getLabel(item).length());
        return this.maxLabelLengthCache = max;
    }

    @Override
    protected int[] calculateInnerSize() {
        int width = this.getTitle().length();
        width = Math.max(width, this.getMaxLabelLength() + 4);
        return new int[] { width, 4 + this.getItemCount() };
    }

    @Override
    protected void drawContent(@NotNull ColoredCharCanvas canvas, boolean compressed) {
        final int w = canvas.getWidth();
        final int h = canvas.getHeight();

        canvas.setColumn(0, '│');
        canvas.setColumn(w - 1, '│');
        canvas.setChar(0, 0, ' ');
        canvas.setChar(w - 1, 0, ' ');

        canvas.setForegroundColorRect(0, 0, w, 1, CharColor.MAGENTA_BRIGHT);
        final CharSequence title = this.getTitle();
        if (compressed && (title.length() > w)) {
            canvas.type(0, 0, title.subSequence(0, w));
        } else {
            canvas.typeCentered(0, 0, title, w);
        }

        canvas.setRow(1, '─');
        canvas.setChar(0, 1, '╭');
        canvas.setChar(w - 1, 1, '╮');

        canvas.setRow(h - 1, '─');
        canvas.setChar(0, h - 1, '╰');
        canvas.setChar(w - 1, h - 1, '╯');

        if (compressed && (w < (this.getMaxLabelLength() + 4))) {
            this.drawLabels(canvas.subCanvas(1, 2, w - 2, h - 3), true);
        } else {
            this.drawLabels(canvas.subCanvas(2, 2, w - 4, h - 3), compressed);
        }
    }

    protected void drawLabels(@NotNull ColoredCharCanvas canvas, boolean compressed) {
        final int w = canvas.getWidth();
        final int h = canvas.getHeight();

        final List<CharSequence> labels = this.getLabels();
        final int size = labels.size();
        int selection = this.selection;
        if (selection < 0) {
            this.selection = selection = size - 1;
        } else if (selection >= size) {
            this.selection = selection = 0;
        }

        int index = Math.floorDiv(selection, h) * h;
        int y = 0;

        CharSequence label;
        while (true) {
            if (index == selection) {
                canvas.setBackgroundColorRect(0, y, w, 1, CharColor.YELLOW);
            }

            label = labels.get(index);
            if (index == 0) {
                canvas.setForegroundColorRect(0, y, w, 1, index == selection ? CharColor.YELLOW_BRIGHT : CharColor.BLACK_BRIGHT);
                canvas.typeCentered(0, y, label, w);
            } else {
                if (compressed && (label.length() > w)) {
                    canvas.type(0, y, label.subSequence(0, w));
                    for (int dx=-1; dx > -4; dx--) {
                        canvas.setChar(w + dx, y, '.');
                    }
                } else {
                    canvas.type(0, y, label);
                }
            }

            if ((++index) >= size) break;
            if ((++y) >= h) break;
        }
    }

    @Override
    public boolean onInput(@NotNull TUI tui, @NotNull ConfigStage stage, char input) {
        if (!(input == ((char) 32) || input == ((char) 13) || input == ((char) 10))) return false;
        if (this.selection < 0) {
            return false;
        } else if (this.selection == 0) {
            this.onCancel(tui, stage);
            return true;
        } else {
            int index = this.selection - 1;
            if (index >= this.getItemCount()) return false;
            T item = this.getItems().get(index);
            this.onSubmit(tui, stage, item);
            return true;
        }
    }

    @Override
    public void onInputArrowKey(@NotNull TUI tui, @NotNull ConfigStage stage, @NotNull TUIArrowKey key) {
        switch (key) {
            case UP:
                this.selection--;
                break;
            case DOWN:
                this.selection++;
                break;
        }
    }

}
