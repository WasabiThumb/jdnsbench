package io.github.wasabithumb.jdnsbench.util;

import org.jetbrains.annotations.NotNull;

import java.nio.CharBuffer;

public final class IPUtil {

    public static boolean isValidV6(final @NotNull CharSequence ip) {
        CharBuffer buf = CharBuffer.wrap(ip);
        int count = 0;
        char c;
        for (int i=0; i < ip.length(); i++) {
            c = ip.charAt(i);
            if (c == ':') {
                if ((++count) == 2) {
                    final int preCount = isValidV60(buf.subSequence(0, i - 1), true);
                    if (preCount == -1) return false;
                    final int postCount = isValidV60(buf.subSequence(i + 1, ip.length()), true);
                    if (postCount == -1) return false;
                    return (preCount + postCount) < 8;
                }
            } else {
                count = 0;
            }
        }
        return isValidV60(buf, false) == 8;
    }

    private static int isValidV60(final CharBuffer ip, boolean allowEmpty) {
        final int len = ip.length();
        if (len == 0) return allowEmpty ? 0 : -1;
        int nDigits = 0;
        int nBlocks = 1;
        char c;
        for (int i=0; i < len; i++) {
            c = ip.charAt(i);
            if (Character.digit(c, 16) != -1) {
                if ((nDigits++) == 4) return -1;
            } else if (c == ':') {
                if (nDigits == 0) return -1;
                nDigits = 0;
                nBlocks++;
            } else {
                return -1;
            }
        }
        if (nDigits == 0) return -1;
        return nBlocks;
    }

    public static boolean isValidV4(final @NotNull CharSequence ip) {
        int nDots = 0;
        int nDigits = 0;
        char[] digits = new char[3];
        char c;
        for (int i = 0; i < ip.length(); i++) {
            c = ip.charAt(i);
            if (c >= '0' && c <= '9') {
                if (nDigits == 3) return false;
                digits[nDigits++] = c;
            } else if (c == '.') {
                if (isValidV40(nDigits, digits)) return false;
                nDigits = 0;
                if ((++nDots) == 4) return false;
            } else {
                return false;
            }
        }
        if (isValidV40(nDigits, digits)) return false;
        return nDots == 3;
    }

    private static boolean isValidV40(int nDigits, char[] digits) {
        if (nDigits == 0) return true;
        int num = 0;
        for (int i = 0; i < nDigits; i++) {
            if (i != 0) num *= 10;
            num += ((int) digits[i]) - ((int) '0');
        }
        return num > 255;
    }

}
