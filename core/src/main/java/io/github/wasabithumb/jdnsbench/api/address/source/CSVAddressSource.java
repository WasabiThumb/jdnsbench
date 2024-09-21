package io.github.wasabithumb.jdnsbench.api.address.source;

import io.github.wasabithumb.jdnsbench.api.address.Address;
import io.github.wasabithumb.jdnsbench.util.CSVReader;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public abstract class CSVAddressSource implements AddressSource {

    @Override
    public @NotNull Collection<Address> get() throws IOException {
        try (InputStream stream = this.getStream();
             InputStreamReader rawReader = new InputStreamReader(stream, StandardCharsets.UTF_8);
             CSVReader reader = new CSVReader(rawReader)
        ) {
            final List<String> header = reader.readRow(this.getExpectedColumns());
            if (header == null) throw new IOException("Missing header in CSV stream");
            final int columns = header.size();

            final int addressIndex = header.indexOf(this.getAddressKey());
            if (addressIndex == -1)
                throw new IOException("CSV header is missing required key: " + this.getAddressKey());

            final String[] labelKeys = this.getLabelKeys();
            final int[] labelIndices = new int[labelKeys.length];
            String labelKey;
            for (int i=0; i < labelKeys.length; i++) {
                labelKey = labelKeys[i];
                labelIndices[i] = header.indexOf(labelKey);
            }

            final List<Address> ret = new LinkedList<>();
            List<String> row;
            Address address;
            int counter = 1;
            while ((row = reader.readRow(columns)) != null) {
                address = this.rowToAddress(counter++, row, addressIndex, labelIndices);
                ret.add(address);
            }

            return ret;
        }
    }

    protected @NotNull Address rowToAddress(
            int line,
            @NotNull List<String> row,
            int addressIndex,
            int[] labelIndices
    ) throws IOException {
        final int len = row.size();
        if (addressIndex >= len) throw new IOException("Malformed CSV row @ line " + line);

        Address address;
        try {
            address = Address.of(row.get(addressIndex));
        } catch (IllegalArgumentException ex) {
            throw new IOException("Malformed CSV row @ line " + line, ex);
        }

        String label;
        for (int labelIndex : labelIndices) {
            if (labelIndex == -1 || labelIndex >= len) continue;
            label = row.get(labelIndex);
            if (label.isBlank()) continue;
            return address.label(label);
        }

        return address;
    }

    //

    protected abstract @NotNull InputStream getStream() throws IOException;

    protected @NotNull String getAddressKey() {
        return "ip_address";
    }

    protected @NotNull String[] getLabelKeys() {
        return new String[] {
                "as_org",
                "name",
                "city"
        };
    }

    protected int getExpectedColumns() {
        return 12;
    }

}
