package io.github.wasabithumb.jdnsbench.tui.stage.impl;

import io.github.wasabithumb.jdnsbench.tui.TUI;
import io.github.wasabithumb.jdnsbench.tui.TUIArrowKey;
import io.github.wasabithumb.jdnsbench.tui.TUIContext;
import io.github.wasabithumb.jdnsbench.tui.stage.TUIStage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jline.jansi.Ansi;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.zip.InflaterInputStream;

public class MainStage implements TUIStage {

    private static final String[] OPTION_TEXTS = new String[] {
            "Run Benchmark",
            "Configuration",
            "Visit Homepage",
            "Exit"
    };
    private static final int OPTION_TEXT_MAX_LENGTH = OPTION_TEXTS[2].length();
    private static final int MIN_WIDTH = 47;
    private static final int MIN_HEIGHT = 21;
    private static final String[] LOGO_TEXTS = new String[5];
    static {
        final byte[] LOGO_DATA = new byte[] {
                 120, -100, -107,   80,   65,   14,  -64,   32,    8,  -29,   41,  -34,  -44,  -61,  -26, -121,   72,
                 -36,   67,  120,   -4,  -76,   84,  -35, -106,  108,  -55,   66,  -88,   80,   75,   49,   74, -112,
                  30,   21,   89,  113, -122,   11,   14,  110,  -35,  -67,  -59,  -70,   21,   96,  106,   76,   38,
                 -93,   98,   19,   11,  -35, -116,   51,   -7,  -74,  101,  -19,  118, -123,  -79,  119,   79, -101,
                -103,   89,  -19,  114,  -64,  -39,  -25, -107,  -13, -118,   45,   27,   95,   16,   39,  -29,  -98,
                -111,   74,    1,   83,  -72,  -91,  -94,   54,  104,   20,   56,  -72,  113,   42,   43,  -41,  116,
                 -44,   71,  111,  -51,  -45,  -88,   -2,   -6,  -85,  127,   33,   39,   66,   65,   49,   -7
        };

        char[] buf = new char[41];
        int h1 = 0;
        int h2 = 0;

        try (ByteArrayInputStream bis = new ByteArrayInputStream(LOGO_DATA)) {
            try (InflaterInputStream iis = new InflaterInputStream(bis)) {
                int hi;
                int lo;
                char c;

                while ((hi = iis.read()) != -1) {
                    lo = iis.read();
                    if (lo == -1) throw new AssertionError();
                    c = (char) ((hi << 8) | lo);
                    if (c == '\0') {
                        LOGO_TEXTS[h2++] = new String(buf, 0, h1);
                        h1 = 0;
                    } else {
                        buf[h1++] = c;
                    }
                }
            }
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    private final TUIContext context;
    private int selectedOption = 0;

    public MainStage(final TUIContext context) {
        this.context = context;
    }

    public MainStage() {
        this(new TUIContext());
    }

    //

    @Override
    public void draw(@NotNull TUI tui) {
        final int width = tui.width();
        final int height = tui.height();

        if (width < MIN_WIDTH || height < MIN_HEIGHT) {
            System.out.println(tui.ansi().fgBrightCyan().a("jDNSBench").reset());
            System.out.println(tui.ansi().fgYellow().a("(Tiny mode)").reset());
            System.out.println(tui.ansi().a("Select an option:").reset());
            for (int i=0; i < OPTION_TEXTS.length; i++) {
                Ansi ansi = tui.ansi()
                        .fgRgb(0x7f7f7f)
                        .a(" [")
                        .fgBrightMagenta()
                        .a(i + 1)
                        .fgRgb(0x7f7f7f)
                        .a("] ");
                if (i == this.selectedOption) {
                    ansi = ansi.fgMagenta();
                } else {
                    ansi = ansi.reset();
                }
                System.out.println(ansi.a(OPTION_TEXTS[i]).reset());
            }
            return;
        }

        char[] chars = new char[width];
        Arrays.fill(chars, '█');
        final String caps = new String(chars);
        for (int i=1; i < (chars.length - 1); i++) chars[i] = ' ';

        for (int y=1; y <= height; y++) {
            if (y == 1 || y == height) {
                System.out.print(caps);
            } else {
                StyledLine line = this.getLineForRow(y - 1, height - 2);
                if (line != null) {
                    final int padding = width - 2 - line.length();
                    if (padding < 0) {
                        System.out.print(tui.ansi().bold().bgRed().a("ENOSP").reset());
                        continue;
                    }
                    final int start = Math.floorDiv(padding, 2) + 1;
                    final int end = start + line.length();
                    System.out.print(new String(chars, 0, start));
                    System.out.print(line.content);
                    System.out.print(new String(chars, end, width - end));
                } else {
                    System.out.print(new String(chars));
                }
            }
            if (y < height) System.out.print('\n');
        }
    }

    @Override
    public void onInput(@NotNull TUI tui, char input) {
        int selected = this.selectedOption;
        if (input >= '1' && input <= '4') {
            selected = ((int) input) - ((int) '1');
        } else if (!(input == ((char) 32) || input == ((char) 13) || input == ((char) 10))) {
            return;
        }
        switch (selected) {
            case 0:
                tui.setStage(new BenchmarkStage(this.context));
                break;
            case 1:
                tui.setStage(new ConfigStage(this.context));
                break;
            case 2:
                tui.setStage(new HomepageStage());
                break;
            case 3:
                tui.shutdown();
                break;
        }
    }

    @Override
    public void onInputArrowKey(@NotNull TUI tui, @NotNull TUIArrowKey key) {
        if (key == TUIArrowKey.DOWN || key == TUIArrowKey.RIGHT) {
            if (++this.selectedOption >= OPTION_TEXTS.length) {
                this.selectedOption = 0;
            }
        } else if (key == TUIArrowKey.UP || key == TUIArrowKey.LEFT) {
            if (--this.selectedOption < 0) {
                this.selectedOption = OPTION_TEXTS.length - 1;
            }
        }
    }

    private @Nullable StyledLine getLineForRow(int row, int height) {
        final int instructStart = Math.floorDiv(height, 2) - 1;

        switch (row - instructStart) {
            case 0:
                return cb("Made by Wasabi <3");
            case 1:
                return cb("Use ↑↓ or digits to select an option");
            case 2:
                return cb("ESC to exit");
        }

        final int optionStart = height - (OPTION_TEXTS.length << 1);
        if (row >= optionStart && row < height) {
            int optionIndex = row - optionStart;
            if ((optionIndex & 1) == 1) return null;
            optionIndex >>= 1;

            String text = OPTION_TEXTS[optionIndex];
            int textLength = text.length();
            final int pad = OPTION_TEXT_MAX_LENGTH - textLength + 4;
            final int padRight = Math.floorDiv(pad, 2) + 1;
            final int padLeft = pad - padRight;
            text = " ".repeat(padLeft) + text + " ".repeat(padRight);

            if (optionIndex == this.selectedOption) {
                text = " ▶ " + text;
                return cd(text);
            } else {
                text = " " + ((char) (optionIndex + '1')) + " " + text;
                return cc(text);
            }
        }

        final int logoStart = Math.floorDiv(height, 5);
        final int logoRow = row - logoStart;
        if (logoRow >= 0 && logoRow < 5) {
            return ca(LOGO_TEXTS[logoRow]);
        }

        return null;
    }

    private @NotNull StyledLine ca(@NotNull String line) {
        return new StyledLine(line.length() + 4, Ansi.ansi().fgBrightCyan().bgRgb(0).a("  ").a(line).a("  ").reset().toString());
    }

    private @NotNull StyledLine cb(@NotNull String line) {
        return new StyledLine(line.length(), Ansi.ansi().bold().fgRgb(127, 127, 127).a(line).reset().toString());
    }

    private @NotNull StyledLine cc(@NotNull String line) {
        return new StyledLine(line.length(), Ansi.ansi().fgRgb(255, 255, 255).bgRgb(64, 64, 64).a(line).reset().toString());
    }

    private @NotNull StyledLine cd(@NotNull String line) {
        return new StyledLine(line.length(), Ansi.ansi().fgRgb(255, 255, 255).bgBrightMagenta().a(line).reset().toString());
    }

    private record StyledLine(int length, String content) { }

}
