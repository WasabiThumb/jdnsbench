package io.github.wasabithumb.jdnsbench.util;

import java.nio.CharBuffer;

public final class AnsiUtil {

    public static final CharSequence ESC_ENABLE_BUF;
    public static final CharSequence ESC_DISABLE_BUF;
    public static final CharSequence ESC_CURSOR_INVISIBLE;
    public static final CharSequence ESC_CURSOR_VISIBLE;
    public static final CharSequence ESC_DIM;
    public static final CharSequence ESC_DIM_OFF;
    static {
        final CharSequence PREFIX = CharBuffer.wrap(new char[] { (char) 27, '[' });
        ESC_ENABLE_BUF       = new JoinedCharSequence(PREFIX, "?1049h");
        ESC_DISABLE_BUF      = new JoinedCharSequence(PREFIX, "?1049l");
        ESC_CURSOR_INVISIBLE = new JoinedCharSequence(PREFIX, "?25l");
        ESC_CURSOR_VISIBLE   = new JoinedCharSequence(PREFIX, "?25h");
        ESC_DIM              = new JoinedCharSequence(PREFIX, "2m");
        ESC_DIM_OFF          = new JoinedCharSequence(PREFIX, "22m");
    }

}
