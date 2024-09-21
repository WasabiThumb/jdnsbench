package io.github.wasabithumb.jdnsbench.asset.loader;

import io.github.wasabithumb.jdnsbench.asset.exception.AssetLoadException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;

class DevAssetLoader extends AssetLoader {

    private static final String[] PATH_TO_BUILD = new String[] { "main", "java", "classes" };
    private static final String ERR_BAD_PATH = "Classpath does not meet expectations";

    private final File codeSource;
    public DevAssetLoader(@NotNull File codeSource) {
        this.codeSource = codeSource;
    }

    private @NotNull File getResourcesDir() throws AssetLoadException {
        File head = codeSource;
        for (String name : PATH_TO_BUILD) {
            if (!head.getName().equals(name)) throw new AssetLoadException(ERR_BAD_PATH);
            head = head.getParentFile();
            if (head == null) throw new AssetLoadException(ERR_BAD_PATH);
        }
        // head should be ${projectDir}/app/build
        head = new File(head, "resources/main");
        if (!head.isDirectory()) throw new AssetLoadException(ERR_BAD_PATH);
        return head;
    }

    @Override
    public boolean loadLibrary(@NotNull String name) throws AssetLoadException {
        final File source = new File(this.getResourcesDir(), this.nativeLibraryFileName(name));
        if (!source.exists()) return false;

        try {
            System.load(source.getAbsolutePath());
        } catch (LinkageError | SecurityException e) {
            throw new AssetLoadException("Failed to load shared library @ " + source.getAbsolutePath(), e);
        }
        return true;
    }

    @Override
    public @Nullable InputStream loadStream(@NotNull String name) throws AssetLoadException {
        final File source = new File(this.getResourcesDir(), name);
        if (!source.isFile()) return null;
        try {
            return new FileInputStream(source);
        } catch (IOException e) {
            throw new AssetLoadException("Failed to open file \"" + source.getAbsolutePath() + "\" for reading", e);
        }
    }
}
