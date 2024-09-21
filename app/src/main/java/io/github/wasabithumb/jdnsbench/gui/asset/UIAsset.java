package io.github.wasabithumb.jdnsbench.gui.asset;

import io.github.wasabithumb.jdnsbench.asset.exception.AssetLoadException;
import io.github.wasabithumb.jdnsbench.asset.loader.AssetLoader;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.locks.StampedLock;

class UIAsset<T> {

    private final UIAssetType<T> type;
    private final String path;
    private final StampedLock lock;
    private UIAssetState state = UIAssetState.UNLOADED;
    private byte[] preloaded = null;
    private T loaded = null;
    public UIAsset(final UIAssetType<T> type, final String path) {
        this.type = type;
        this.path = path;
        this.lock = new StampedLock();
    }

    public @NotNull UIAssetType<T> type() {
        return this.type;
    }

    public @NotNull String path() {
        return this.path;
    }

    public void preload(@NotNull AssetLoader loader) throws AssetLoadException {
        long stamp = this.lock.readLock();
        try {
            if (this.state != UIAssetState.UNLOADED) return;
            stamp = this.lock.tryConvertToWriteLock(stamp);

            try (InputStream is = this.getStream(loader)) {
                this.preloaded = is.readAllBytes();
            } catch (IOException e) {
                throw new AssetLoadException("Failed to preload asset \"" + this.path + "\"", e);
            }
            this.state = UIAssetState.PRELOADED;
        } finally {
            this.lock.unlock(stamp);
        }
    }

    public @NotNull T get(@NotNull AssetLoader loader) throws AssetLoadException {
        long stamp = this.lock.readLock();
        try {
            switch (this.state) {
                case UNLOADED:
                    stamp = this.lock.writeLock();
                    T value1;
                    try (InputStream is = this.getStream(loader)) {
                        value1 = this.type.readFromStream(is);
                    } catch (IOException e) {
                        this.wrapGenericIOError(e);
                        return null;
                    }
                    this.state = UIAssetState.LOADED;
                    this.loaded = value1;
                    return value1;
                case PRELOADED:
                    stamp = this.lock.writeLock();
                    T value2;
                    try {
                        value2 = this.type.readFromBytes(this.preloaded);
                    } catch (IOException e) {
                        this.wrapGenericIOError(e);
                        return null;
                    }
                    this.state = UIAssetState.LOADED;
                    this.preloaded = null;
                    this.loaded = value2;
                    return value2;
                case LOADED:
                    return this.loaded;
            }
        } finally {
            this.lock.unlock(stamp);
        }
        throw new IllegalStateException();
    }

    private @NotNull InputStream getStream(@NotNull AssetLoader loader) {
        InputStream is = loader.loadStream(this.path);
        if (is == null) throw new AssetLoadException("Asset \"" + this.path + "\" not found");
        return is;
    }

    @Contract("_ -> fail")
    private void wrapGenericIOError(@NotNull IOException e) throws AssetLoadException {
        throw new AssetLoadException("Failed to read asset \"" + this.path + "\" as an instance of " +
                this.type.dataClass().getName(), e);
    }

}
