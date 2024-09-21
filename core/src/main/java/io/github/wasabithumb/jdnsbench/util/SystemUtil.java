package io.github.wasabithumb.jdnsbench.util;

public final class SystemUtil {

    public static final boolean IS_WINDOWS;

    public static final boolean IS_LINUX;

    /**
     * Windows major version. In practice, will be one of: 7, 8, 10, 11
     */
    public static final int WINDOWS_MAJOR_VERSION;

    static {
        final String os = System.getProperty("os.name").toLowerCase();
        int whereWindows = os.indexOf("windows");
        IS_WINDOWS = (whereWindows != -1);
        IS_LINUX = os.contains("linux");

        int windowsVersion = 7;
        if (IS_WINDOWS) {
            int head = whereWindows + 7;
            if (head < (os.length() - 1) && os.charAt(head) == ' ') {
                int end = ++head;
                while (end < os.length()) {
                    if (os.charAt(end) == ' ' || os.charAt(end) == '.') break;
                    end++;
                }
                String versionStr = os.substring(head, end);
                try {
                    windowsVersion = Integer.parseInt(versionStr);
                } catch (NumberFormatException ignored) { }
            }
        }
        WINDOWS_MAJOR_VERSION = windowsVersion;
    }

}
