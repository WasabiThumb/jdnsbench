package io.github.wasabithumb.jdnsbench.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class CSVReader extends BufferedReader {

    public CSVReader(@NotNull Reader in) {
        super(in);
    }

    public @Nullable List<String> readRow(int expectedColumns) throws IOException {
        String rawRow = this.readLine();
        if (rawRow == null) return null;

        final StringBuilder sb = new StringBuilder(Math.floorDiv(rawRow.length() << 1, expectedColumns));
        final List<String> row = new ArrayList<>(expectedColumns);
        int i = 0;
        boolean loop = true;
        boolean escaped = false;
        char c;

        while (loop) {
            if (i < rawRow.length()) {
                c = rawRow.charAt(i++);

                if (c == '\"') {
                    if (escaped) {
                        if (i < rawRow.length() && rawRow.charAt(i + 1) == '\"') {
                            i++;
                            sb.append(c);
                        } else {
                            escaped = false;
                        }
                    } else {
                        escaped = true;
                    }
                    continue;
                } else if (escaped || c != ',') {
                    sb.append(c);
                    continue;
                }
            } else {
                loop = false;
            }
            row.add(sb.toString());
            sb.setLength(0);
        }

        return row;
    }

    public @Nullable List<String> readRow() throws IOException {
        return this.readRow(16);
    }

}
