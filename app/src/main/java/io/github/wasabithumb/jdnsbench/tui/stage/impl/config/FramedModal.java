package io.github.wasabithumb.jdnsbench.tui.stage.impl.config;

import io.github.wasabithumb.jdnsbench.tui.TUI;
import io.github.wasabithumb.jdnsbench.tui.bitmap.CharColor;
import io.github.wasabithumb.jdnsbench.tui.bitmap.ColoredCharCanvas;
import io.github.wasabithumb.jdnsbench.tui.stage.impl.ConfigStage;
import org.jetbrains.annotations.NotNull;
import org.jline.jansi.Ansi;

abstract class FramedModal implements ConfigStageModal {

    @Override
    public void draw(@NotNull TUI tui, @NotNull ConfigStage stage, int cw, int ch) {
        final int[] innerSize = this.calculateInnerSize();
        int innerWidth = innerSize[0];
        int innerHeight = innerSize[1];

        boolean compressed = false;
        int outerWidth = innerWidth + 4;
        int outerHeight = innerHeight + 4;
        if (outerWidth > cw) {
            outerWidth = cw;
            innerWidth = outerWidth - 4;
            compressed = true;
        }
        if (outerHeight > ch) {
            outerHeight = ch;
            innerHeight = outerHeight - 4;
            compressed = true;
        }

        final int x = Math.floorDiv(cw - outerWidth, 2);
        final int y = Math.floorDiv(ch - outerHeight, 2);

        ColoredCharCanvas frame = this.createFrame(outerWidth, outerHeight);
        ColoredCharCanvas inner = frame.subCanvas(2, 2, innerWidth, innerHeight);
        this.drawContent(inner, compressed);

        System.out.print(Ansi.ansi().cursor(1, 1).cursorRight(x).cursorDown(y).bold());
        for (int dy=0; dy < outerHeight; dy++) {
            if (dy != 0) System.out.print(Ansi.ansi().cursorLeft(outerWidth).cursorDown(1));
            System.out.print(frame.getRow(dy));
        }
        System.out.print(Ansi.ansi().boldOff().cursor(1, 1));
    }

    protected abstract int[] calculateInnerSize();

    protected abstract void drawContent(@NotNull ColoredCharCanvas canvas, boolean compressed);

    protected ColoredCharCanvas createFrame(int outerWidth, int outerHeight) {
        ColoredCharCanvas sub = new ColoredCharCanvas(outerWidth, outerHeight);
        sub.setBackgroundColorRect(0, 0, outerWidth, outerHeight, CharColor.BLACK);
        sub.setForegroundColorRect(0, 0, outerWidth, outerHeight, CharColor.DEFAULT);
        sub.fill(' ');
        sub.setRow(0, '━');
        sub.setRow(outerHeight - 1, '━');
        sub.setColumn(0, '┃');
        sub.setColumn(outerWidth - 1, '┃');
        sub.setChar(0, 0, '┏');
        sub.setChar(outerWidth - 1, 0, '┓');
        sub.setChar(0, outerHeight - 1, '┗');
        sub.setChar(outerWidth - 1, outerHeight - 1, '┛');
        return sub;
    }

}
