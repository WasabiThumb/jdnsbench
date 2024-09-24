package io.github.wasabithumb.jdnsbench.tui.stage.impl;

import io.github.wasabithumb.jdnsbench.tui.TUI;
import io.github.wasabithumb.jdnsbench.tui.TUIArrowKey;
import io.github.wasabithumb.jdnsbench.tui.bitmap.CharColor;
import io.github.wasabithumb.jdnsbench.tui.bitmap.ColoredCharCanvas;
import io.github.wasabithumb.jdnsbench.tui.stage.TUIStage;
import io.github.wasabithumb.jdnsbench.util.AnsiUtil;
import io.github.wasabithumb.jdnsbench.util.JoinedCharSequence;
import org.jetbrains.annotations.NotNull;

import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.List;

public class MissingLibraryStage implements TUIStage {

    private static final String DETAIL = " is required to run this application. Follow the instructions for your distro below or press ESC to exit.";
    private static final String HEADER = "Missing Library";
    private static final int HEADER_WIDTH = HEADER.length() + 4;
    private static final int MIN_WIDTH = 32;
    private static final int MIN_HEIGHT = 14;

    private final CharSequence detail;
    private final List<Instructions> instructions = new ArrayList<>();
    private int selection = 0;
    private boolean expanded = false;
    public MissingLibraryStage(String libName) {
        this.detail = new JoinedCharSequence(libName, DETAIL);
    }

    public void addInstructions(@NotNull String distro, @NotNull String... commands) {
        this.instructions.add(new Instructions(distro, commands));
    }

    @Override
    public void draw(@NotNull TUI tui) {
        final int w = tui.width();
        final int h = tui.height();

        if (w < MIN_WIDTH || h < MIN_HEIGHT) {
            this.tooSmall(tui);
            return;
        }

        if (this.expanded) {
            this.drawInstructions(w, h, this.instructions.get(this.selection));
            return;
        }

        final int padLeft = Math.floorDiv(w - HEADER_WIDTH, 2);
        final int padRight = w - padLeft;
        if (padLeft > 0) System.out.print(" ".repeat(padLeft));
        System.out.print(tui.ansi()
                .a(AnsiUtil.ESC_BLINK)
                .fgBrightRed()
                .a("!")
                .a(AnsiUtil.ESC_BLINK_OFF)
                .fgRed()
                .a(' ')
                .a(HEADER)
                .a(' ')
                .a(AnsiUtil.ESC_BLINK)
                .fgBrightRed()
                .a("!")
                .a(AnsiUtil.ESC_BLINK_OFF)
                .reset()
        );
        if (padRight > 0) System.out.print(" ".repeat(padRight));
        System.out.print(tui.ansi().a('\n').cursor(2, 1));

        final ColoredCharCanvas canvas = new ColoredCharCanvas(w, h - 1);
        int ly = this.drawDetail(canvas);
        canvas.setForegroundColorRect(0, 0, w, ly, CharColor.YELLOW_BRIGHT);
        this.drawList(canvas.subCanvas(0, ly + 1, w, h - 2 - ly));
        canvas.print();
    }

    private int drawDetail(@NotNull ColoredCharCanvas canvas) {
        return this.drawWrapping(canvas, this.detail, true);
    }

    private int drawWrapping(@NotNull ColoredCharCanvas canvas, @NotNull CharSequence text, boolean centered) {
        final int w = canvas.getWidth();
        int y = 0;
        final int len = text.length();

        StringBuilder line = new StringBuilder(w);
        CharSequence word;
        boolean end;

        int wordStart = 0;
        for (int i=0; i <= len; i++) {
            if ((end = (i == len)) || text.charAt(i) == ' ') {
                word = CharBuffer.wrap(text, wordStart, i);
                wordStart = i + 1;

                final int totalLen = line.isEmpty() ? word.length() : line.length() + 1 + word.length();
                if (totalLen > w) {
                    if (centered) {
                        canvas.typeCentered(0, y++, line, w);
                    } else {
                        canvas.type(0, y++, line);
                    }
                    line.setLength(word.length());
                    for (int z=0; z < word.length(); z++) line.setCharAt(z, word.charAt(z));
                } else {
                    line.append(' ').append(word);
                }
                if (end) {
                    if (centered) {
                        canvas.typeCentered(0, y++, line, w);
                    } else {
                        canvas.type(0, y++, line);
                    }
                }
            }
        }

        return y;
    }

    private void drawList(@NotNull ColoredCharCanvas canvas) {
        final int ow = canvas.getWidth();
        final int oh = canvas.getHeight();

        canvas.setRow(0, '─');
        canvas.setRow(oh - 1, '─');

        canvas.setColumn(0, '│');
        canvas.setColumn(ow - 1, '│');

        canvas.setChar(0, 0, '┌');
        canvas.setChar(ow - 1, 0, '┐');
        canvas.setChar(0, oh - 1, '└');
        canvas.setChar(ow - 1, oh - 1, '┘');

        final int w = ow - 2;
        final int h = oh - 2;
        ColoredCharCanvas inner = canvas.subCanvas(1, 1, w, h);

        int selection = this.selection;
        if (selection < 0) {
            this.selection = selection = this.instructions.size() - 1;
        } else if (selection >= this.instructions.size()) {
            this.selection = selection = 0;
        }
        int page = Math.floorDiv(selection, h);

        int index = page * h;
        String label;
        for (int y=0; y < h; y++) {
            if (index >= this.instructions.size()) break;
            label = this.instructions.get(index).distro;

            if (index == selection) {
                inner.setBackgroundColorRect(0, y, w, 1, CharColor.BLUE);
            }
            if (label.length() > w) {
                inner.type(0, y, label.substring(0, w));
                for (int dx=-1; dx >= -4; dx--) inner.setChar(w + dx, y, '.');
            } else {
                inner.type(0, y, label);
            }

            index++;
        }
    }

    private void drawInstructions(final int w, final int h, final @NotNull Instructions data) {
        ColoredCharCanvas canvas = new ColoredCharCanvas(w, h);
        canvas.setBackgroundColorRect(0, 0, w, 1, CharColor.MAGENTA);

        String distro = data.distro;
        if (distro.length() > w) {
            canvas.type(0, 0, distro.substring(0, w));
        } else {
            canvas.typeCentered(0, 0, distro, w);
        }

        final int ih = h - 1;
        ColoredCharCanvas digits = canvas.subCanvas(0, 1, w, ih);
        digits.setColumn(2, '│');
        digits.setForegroundColorRect(0, 0, 2, ih, CharColor.BLACK_BRIGHT);

        final int tw = w - 3;
        ColoredCharCanvas text = canvas.subCanvas(3, 1, tw, ih);

        int y = 0;
        String line;
        for (int i=0; i < data.commands.length; i++) {
            line = data.commands[i];
            int sy = y;
            y += this.drawWrapping(text.subCanvas(0, y, tw, ih - y), line, false);
            if (line.charAt(0) == '#') {
                text.setForegroundColorRect(0, sy, tw, y - sy, CharColor.BLACK_BRIGHT);
            }

            int n = i + 1;
            if (n > 9) digits.setChar(0, sy, Character.forDigit(n / 10, 10));
            digits.setChar(1, sy, Character.forDigit(n % 10, 10));
        }

        canvas.print();
    }

    @Override
    public void onInput(@NotNull TUI tui, char input) {
        if (input == ((char) 32) || input == ((char) 13) || input == ((char) 10)) {
            if (this.expanded) {
                this.expanded = false;
            } else if (this.selection >= 0 && this.selection < this.instructions.size()) {
                this.expanded = true;
            }
        }
    }

    @Override
    public void onInputArrowKey(@NotNull TUI tui, @NotNull TUIArrowKey key) {
        if (this.expanded) return;
        switch (key) {
            case UP:
                this.selection--;
                break;
            case DOWN:
                this.selection++;
                break;
        }
    }

    //

    private record Instructions(String distro, String[] commands) { }

}
