package io.github.wasabithumb.jdnsbench.tui.stage.impl.config;

import io.github.wasabithumb.jdnsbench.api.address.provider.AddressProvider;
import io.github.wasabithumb.jdnsbench.api.address.source.AddressSource;
import io.github.wasabithumb.jdnsbench.api.address.source.AddressSources;
import io.github.wasabithumb.jdnsbench.asset.AppResources;
import io.github.wasabithumb.jdnsbench.tui.TUI;
import io.github.wasabithumb.jdnsbench.tui.stage.impl.ConfigStage;
import io.github.wasabithumb.jdnsbench.util.CSVReader;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

class AddIPSourceModal extends DropdownModal<AddressSource> {

    private static final List<CountryData> COUNTRY_DATA;
    static {
        List<CountryData> data = new ArrayList<>(193);
        try (InputStream stream = AppResources.getStreamAssert("data/countries.csv");
             InputStreamReader rawReader = new InputStreamReader(stream, StandardCharsets.UTF_8);
             CSVReader reader = new CSVReader(rawReader)
        ) {
            int index = 0;
            List<String> row;
            while ((row = reader.readRow()) != null) {
                if (row.size() != 2)
                    throw new AssertionError("Row #" + index + " has wrong number of columns");
                data.add(new CountryData(row.get(0), row.get(1)));
                index++;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        COUNTRY_DATA = data;
    }

    private final List<AddressSource> sources;
    private final int maxLength;
    public AddIPSourceModal() {
        List<AddressSource> sources = new ArrayList<>(COUNTRY_DATA.size() + 2);
        int maxLength = 0;
        AddressSource source;

        source = AddressSources.defaults();
        maxLength = Math.max(maxLength, source.label().length());
        sources.add(source);

        source = AddressSources.all();
        maxLength = Math.max(maxLength, source.label().length());
        sources.add(source);

        for (CountryData data : COUNTRY_DATA) {
            source = AddressSources.country(data.code, data.name);
            maxLength = Math.max(maxLength, source.label().length());
            sources.add(source);
        }

        this.sources = sources;
        this.maxLength = maxLength;
    }

    @Override
    protected @NotNull CharSequence getTitle() {
        return "IP SOURCE";
    }

    @Override
    protected @NotNull List<AddressSource> getItems() {
        return this.sources;
    }

    @Override
    protected @NotNull CharSequence getLabel(@NotNull AddressSource item) {
        return item.label();
    }

    @Override
    protected int getMaxLabelLength() {
        return this.maxLength;
    }

    @Override
    protected void onSubmit(@NotNull TUI tui, @NotNull ConfigStage stage, @NotNull AddressSource selection) {
        stage.addresses.add(AddressProvider.of(selection));
    }

    //

    private record CountryData(@NotNull String code, @NotNull String name) {}

}
