package io.github.wasabithumb.jdnsbench.asset.exception;

/**
 * Thrown when the asset is presumed to exist, but an error occurred that would prevent loading it. This should
 * not be thrown when an asset does not exist.
 */
public class AssetLoadException extends RuntimeException {

    public AssetLoadException(String message) {
        super(message);
    }

    public AssetLoadException(String message, Throwable cause) {
        super(message, cause);
    }

}
