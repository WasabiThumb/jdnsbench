package io.github.wasabithumb.jdnsbench.api.address.source;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

public class HttpCSVAddressSource extends CSVAddressSource {

    private final URL url;
    private final String label;
    private final int urlHash;
    HttpCSVAddressSource(final URL url, final String label, final String urlString) {
        if (!isHTTP(url)) throw new IllegalArgumentException("URL is not HTTP(S): " + urlString);
        this.url = url;
        this.label = label;
        this.urlHash = urlString.hashCode();
    }

    HttpCSVAddressSource(final URL url, final String urlString) {
        this(url, urlString, urlString);
    }

    public HttpCSVAddressSource(final URL url) throws IllegalArgumentException {
        this(url, url.toString());
    }

    public HttpCSVAddressSource(final String label, final URL url) throws IllegalArgumentException {
        this(url, label, url.toString());
    }

    public HttpCSVAddressSource(final String string) throws IllegalArgumentException {
        this(createURL(string), string);
    }

    public HttpCSVAddressSource(final String label, final String string) throws IllegalArgumentException {
        this(createURL(string), label, string);
    }

    public final @NotNull URL getURL() {
        return this.url;
    }

    //


    @Override
    public @NotNull String label() {
        return this.label;
    }

    @Override
    protected @NotNull InputStream getStream() throws IOException {
        HttpURLConnection con = (HttpURLConnection) this.url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Accept", "text/csv;charset=UTF-8, text/plain;charset=UTF-8;q=0.9, */*;q=0.8");
        con.setRequestProperty("User-Agent", "jDNSBench; wasabithumb.github.io");
        return con.getInputStream();
    }

    //

    @Override
    public int hashCode() {
        return this.urlHash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj instanceof HttpCSVAddressSource other) {
            //noinspection UrlHashCode
            if (this.urlHash == other.urlHash && this.url.equals(other.url)) return true;
        }
        return super.equals(obj);
    }

    //

    private static boolean isHTTP(final URL url) {
        final String protocol = url.getProtocol().toLowerCase(Locale.ROOT);
        if (!protocol.startsWith("http")) return false;
        if (protocol.length() == 4) return true;
        if (protocol.length() != 5) return false;
        return protocol.charAt(4) == 's';
    }

    private static @NotNull URL createURL(final String string) throws IllegalArgumentException {
        try {
            return new URL(string);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e);
        }
    }

}
