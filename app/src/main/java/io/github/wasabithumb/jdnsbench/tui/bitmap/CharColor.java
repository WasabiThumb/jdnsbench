package io.github.wasabithumb.jdnsbench.tui.bitmap;

import org.jline.jansi.Ansi;

public record CharColor(Ansi.Color color, boolean bright) {

    public boolean isDefault() {
        return this.color == Ansi.Color.DEFAULT || this.color == Ansi.Color.WHITE;
    }

    public boolean isSimilar(final CharColor other) {
        final boolean md = this.isDefault();
        final boolean od = other.isDefault();
        if (md != od) return false;
        if (md) return true;
        return this.color == other.color && this.bright == other.bright;
    }

    //

    public static final CharColor DEFAULT = new CharColor(Ansi.Color.DEFAULT, false);

    public static final CharColor BLACK        = new CharColor(Ansi.Color.BLACK, false);
    public static final CharColor BLACK_BRIGHT = new CharColor(Ansi.Color.BLACK, true);

    public static final CharColor RED        = new CharColor(Ansi.Color.RED, false);
    public static final CharColor RED_BRIGHT = new CharColor(Ansi.Color.RED, true);

    public static final CharColor GREEN        = new CharColor(Ansi.Color.GREEN, false);
    public static final CharColor GREEN_BRIGHT = new CharColor(Ansi.Color.GREEN, true);

    public static final CharColor YELLOW        = new CharColor(Ansi.Color.YELLOW, false);
    public static final CharColor YELLOW_BRIGHT = new CharColor(Ansi.Color.YELLOW, true);

    public static final CharColor BLUE        = new CharColor(Ansi.Color.BLUE, false);
    public static final CharColor BLUE_BRIGHT = new CharColor(Ansi.Color.BLUE, true);

    public static final CharColor MAGENTA        = new CharColor(Ansi.Color.MAGENTA, false);
    public static final CharColor MAGENTA_BRIGHT = new CharColor(Ansi.Color.MAGENTA, true);

    public static final CharColor CYAN        = new CharColor(Ansi.Color.CYAN, false);
    public static final CharColor CYAN_BRIGHT = new CharColor(Ansi.Color.CYAN, true);

}
