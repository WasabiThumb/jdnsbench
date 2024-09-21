package io.github.wasabithumb.jdnsbench.util;

import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

@SuppressWarnings("UnstableApiUsage")
public final class ReflectUtil {

    @CheckReturnValue
    public static @NotNull File getCodeSource(@NotNull Class<?> clazz) {
        URI location;
        try {
            location = clazz
                    .getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI();
        } catch (URISyntaxException e) {
            throw new AssertionError("Code source location is an invalid URI", e);
        }
        return new File(location);
    }

    @CheckReturnValue
    public static @NotNull File getCodeSource() {
        return getCodeSource(getCallerClass());
    }

    private static @NotNull Class<?> getCallerClass() {
        final String myClassName = ReflectUtil.class.getName();
        final String threadClassName = Thread.class.getName();
        final StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

        StackTraceElement element;
        String elementName;
        for (int i=1; i < stackTrace.length; i++) {
            element = stackTrace[i];
            elementName = element.getClassName();
            if (myClassName.equals(elementName)) continue;
            if (threadClassName.equals(elementName)) continue;
            try {
                return Class.forName(elementName);
            } catch (ClassNotFoundException e) {
                throw new AssertionError("Cannot find class named by stack trace", e);
            }
        }

        return ReflectUtil.class;
    }

}
