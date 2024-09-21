package io.github.wasabithumb.jdnsbench.asset.loader;

import io.github.wasabithumb.jdnsbench.asset.exception.AssetLoadException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.file.Files;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

class JarAssetLoader extends AssetLoader {

    private final File jarFile;
    public JarAssetLoader(final File jarFile) {
        this.jarFile = jarFile;
    }

    @Override
    public boolean loadLibrary(@NotNull String name) throws AssetLoadException {
        File tempDir;
        try {
            tempDir = Files.createTempDirectory("jdnsbench").toFile();
        } catch (IOException e) {
            throw new AssetLoadException("Error creating temp directory", e);
        }

        final String fileName = this.nativeLibraryFileName(name);
        final File destFile = new File(tempDir, fileName);
        destFile.deleteOnExit();

        try {
            try (InputStream in = this.getInputStream(fileName)) {
                if (in == null) return false;
                try (OutputStream os = new FileOutputStream(destFile)) {
                    byte[] buffer = new byte[8192];
                    int read;
                    while ((read = in.read(buffer)) != -1) os.write(buffer, 0, read);
                    os.flush();
                }
            }
        } catch (IOException e) {
            throw new AssetLoadException("Error piping from JAR file @ " + jarFile.getAbsolutePath(), e);
        }

        try {
            System.load(destFile.getAbsolutePath());
        } catch (LinkageError | SecurityException e) {
            throw new AssetLoadException("Failed to load shared library @ " + destFile.getAbsolutePath(), e);
        }
        return true;
    }

    @Override
    public @Nullable InputStream loadStream(@NotNull String name) throws AssetLoadException {
        try {
            return this.getInputStream(name);
        } catch (IOException e) {
            throw new AssetLoadException("Failed to open JAR file to read \"" + name + "\"", e);
        }
    }

    private @Nullable InputStream getInputStream(@NotNull String fileName) throws IOException {
        ZipInputStream zis = new ZipInputStream(new FileInputStream(this.jarFile));
        boolean close = true;
        try {
            ZipEntry ze;
            String name;
            int start;
            while ((ze = zis.getNextEntry()) != null) {
                if (ze.isDirectory()) continue;
                name = ze.getName();
                if (name.length() < 2) continue;
                start = (name.charAt(0) == '/') ? 1 : 0;
                if (name.regionMatches(start, fileName, 0, name.length() - start)) {
                    close = false;
                    return zis;
                }
            }
        } finally {
            if (close) zis.close();
        }
        return null;
    }

}
