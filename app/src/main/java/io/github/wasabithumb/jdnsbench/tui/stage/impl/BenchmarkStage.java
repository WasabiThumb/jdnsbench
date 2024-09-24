package io.github.wasabithumb.jdnsbench.tui.stage.impl;

import io.github.wasabithumb.jdnsbench.api.address.Address;
import io.github.wasabithumb.jdnsbench.api.bench.JDNSBench;
import io.github.wasabithumb.jdnsbench.api.bench.JDNSBenchJob;
import io.github.wasabithumb.jdnsbench.api.bench.JDNSBenchJobSort;
import io.github.wasabithumb.jdnsbench.tui.TUI;
import io.github.wasabithumb.jdnsbench.tui.TUIArrowKey;
import io.github.wasabithumb.jdnsbench.tui.TUIContext;
import io.github.wasabithumb.jdnsbench.tui.bitmap.CharColor;
import io.github.wasabithumb.jdnsbench.tui.bitmap.ColoredCharCanvas;
import io.github.wasabithumb.jdnsbench.tui.stage.TUIStage;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.List;

import static io.github.wasabithumb.jdnsbench.util.AnsiUtil.ESC_DIM;
import static io.github.wasabithumb.jdnsbench.util.AnsiUtil.ESC_DIM_OFF;

public class BenchmarkStage implements TUIStage {

    private static final char[] PROGESS_STAGES = new char[] { '░', '▒', '▓', '█' };
    private static final DecimalFormat TIMING_FORMAT = new DecimalFormat("0.#");

    private final TUIContext context;
    private JDNSBench bench;
    private JDNSBenchJobSort sort;
    private boolean running;
    private int selection = 0;
    public BenchmarkStage(TUIContext context) {
        this.context = context;
    }

    @Override
    public void onAttach(@NotNull TUI tui) {
        this.bench = new JDNSBench(this.context.options());
        this.sort = JDNSBenchJobSort.STATE;
        this.running = true;
    }

    @Override
    public void onDetach(@NotNull TUI tui) {
        this.bench.close();
    }

    @Override
    public void draw(@NotNull TUI tui) {
        final int w = tui.width();
        final int h = tui.height();
        if (w < 44 || h < 12) {
            this.tooSmall(tui);
            return;
        }

        ColoredCharCanvas canvas = new ColoredCharCanvas(w, h);

        canvas.setRow(1, '─');
        canvas.type(1, 0, "QUIT");
        canvas.setChar(6, 0, '│');
        canvas.setChar(6, 1, '┴');
        canvas.setForegroundColor(1, 0, CharColor.CYAN_BRIGHT);

        String startStop = (this.running) ? "STOP" : "START";
        final int startStopEnd = 9 + startStop.length();
        canvas.type(8, 0, startStop);
        canvas.setChar(startStopEnd, 0, '│');
        canvas.setChar(startStopEnd, 1, '┴');
        canvas.setForegroundColor(8, 0, CharColor.CYAN_BRIGHT);

        final JDNSBench.State state = this.bench.getState();
        final int stateStart = startStopEnd + 2;
        String stateText = state.name();
        CharColor stateColor = CharColor.DEFAULT;
        switch (state) {
            case IDLE:
                stateColor = CharColor.BLACK_BRIGHT;
                break;
            case RESOLVING:
                stateColor = CharColor.YELLOW;
                break;
            case ERROR:
                stateColor = CharColor.RED_BRIGHT;
                break;
            case RUNNING:
                stateColor = CharColor.YELLOW_BRIGHT;
                break;
            case INTERRUPTED:
                stateColor = CharColor.CYAN_BRIGHT;
                stateText = "DONE";
                break;
            case COMPLETE:
                stateColor = CharColor.GREEN_BRIGHT;
                stateText = "DONE";
        }
        final int stateEnd = stateStart + stateText.length() + 1;
        canvas.setForegroundColorRect(stateStart, 0, stateText.length(), 1, stateColor);
        canvas.type(stateStart, 0, stateText);

        final int progressStart = stateEnd + 2;
        final int progressW = w - progressStart - 1;
        final boolean showProgress = (progressW > 7);
        if (showProgress) {
            canvas.setChar(stateEnd, 0, '│');
            canvas.setChar(stateEnd, 1, '┴');
            this.drawProgressBar(canvas.subCanvas(progressStart, 0, progressW, 1));
        }

        canvas.setChar(w - 1, 1, '▄');
        if (this.selection != 0) canvas.setForegroundColor(w - 1, 1, CharColor.BLACK_BRIGHT);
        this.drawTable(canvas.subCanvas(0, 1, w - 1, h - 1), canvas.subCanvas(w - 1, 2, 1, h - 2));

        boolean isError = state == JDNSBench.State.ERROR;
        if (isError) System.out.print(ESC_DIM);
        canvas.print();
        if (isError) {
            System.out.print(ESC_DIM_OFF);
        }
    }

    private void drawTable(ColoredCharCanvas canvas, ColoredCharCanvas scrollBar) {
        final int w = canvas.getWidth();
        canvas.setRow(2, '═');

        final int s1 = Math.floorDiv(w, 3);
        final int s2 = Math.floorDiv(w * 2, 3);

        canvas.setChar(s1, 0, canvas.getChar(s1, 0) == '┴' ? '┼' : '┬');
        canvas.setChar(s1, 1, '│');
        canvas.setChar(s1, 2, '╪');

        canvas.setChar(s2, 0, canvas.getChar(s2, 0) == '┴' ? '┼' : '┬');
        canvas.setChar(s2, 1, '│');
        canvas.setChar(s2, 2, '╪');

        char arrow = (this.sort.isReversed() ? '▲' : '▼');
        int pos;
        pos = canvas.typeCentered(1, 1, "STATE", s1 - 2);
        canvas.setForegroundColor(pos + 1, 1, CharColor.CYAN_BRIGHT);
        if (this.sort.getCategory() == JDNSBenchJobSort.Category.STATE) canvas.setChar(s1 - 2, 1, arrow);

        pos = canvas.typeCentered(s1 + 1, 1, "IP", s2 - s1 - 2);
        canvas.setForegroundColor(pos, 1, CharColor.CYAN_BRIGHT);
        if (this.sort.getCategory() == JDNSBenchJobSort.Category.LABEL) canvas.setChar(s2 - 2, 1, arrow);

        pos = canvas.typeCentered(s2 + 1, 1, "TIME", w - s2 - 2);
        canvas.setForegroundColor(pos + 2, 1, CharColor.CYAN_BRIGHT);
        if (this.sort.getCategory() == JDNSBenchJobSort.Category.TIME) canvas.setChar(w - 2, 1, arrow);

        this.drawTableBody(canvas.subCanvas(0, 3, w, canvas.getHeight() - 3), s1, s2, scrollBar);
    }

    private void drawTableBody(ColoredCharCanvas canvas, int s1, int s2, ColoredCharCanvas scrollbar) {
        final int w = canvas.getWidth();
        final int h = canvas.getHeight();

        canvas.setColumn(s1, '│');
        canvas.setForegroundColorRect(s1, 0, 1, h, CharColor.BLACK_BRIGHT);
        canvas.setColumn(s2, '│');
        canvas.setForegroundColorRect(s2, 0, 1, h, CharColor.BLACK_BRIGHT);

        for (int y=1; y < h; y += 2) {
            canvas.setRow(y, '─');
            canvas.setForegroundColorRect(0, y, w, 1, CharColor.BLACK_BRIGHT);
            canvas.setChar(s1, y, '┼');
            canvas.setChar(s2, y, '┼');
        }

        int total = this.bench.getJobCount();
        int selection = this.selection;
        if (selection >= total) {
            this.selection = selection = 0;
        } else if (selection < 0) {
            this.selection = selection = Math.max(total - 1, 0);
        }

        int page = 0;
        int maxPage = 0;
        if (total != 0) {
            final int n = Math.floorDiv(h + 1, 2);
            page = Math.floorDiv(selection, n);
            maxPage = Math.floorDiv(total - 1, n);
            final int start = page * n;

            List<JDNSBenchJob> jobs = this.bench.getJobs(start, n, this.sort);
            JDNSBenchJob job;
            int y;
            for (int i = 0; i < n; i++) {
                if (i >= jobs.size()) break;
                job = jobs.get(i);
                y = i << 1;
                this.drawJob(canvas.subCanvas(0, y, w, 1), job, s1, s2, selection == (start + i));
            }
        }

        scrollbar.fill('█');
        scrollbar.setForegroundColorRect(0, 0, 1, scrollbar.getHeight(), CharColor.BLACK_BRIGHT);
        final int handleHeight = Math.max(Math.floorDiv(scrollbar.getHeight(), 5), 1);
        final int scrollable = scrollbar.getHeight() - handleHeight;
        final int scroll = page == 0 ? 0 : Math.floorDiv(scrollable * (page + 1), maxPage + 1);
        scrollbar.setForegroundColorRect(0, scroll, 1, handleHeight, CharColor.DEFAULT);
    }

    private void drawJob(ColoredCharCanvas canvas, JDNSBenchJob job, int s1, int s2, boolean selected) {
        final int w = canvas.getWidth();
        if (selected) canvas.setBackgroundColorRect(0, 0, w, 1, CharColor.BLUE);

        JDNSBenchJob.State state = job.getState();
        CharColor stateColor = CharColor.DEFAULT;
        String stateText = state.name();
        switch (state) {
            case RUNNING:
                stateColor = CharColor.YELLOW_BRIGHT;
                stateText += " (" + job.getProgress() + " / " + job.getReps() + ")";
                break;
            case COMPLETE:
                stateColor = CharColor.GREEN_BRIGHT;
                stateText = "DONE";
                break;
            case ERROR:
                stateColor = CharColor.RED;
                break;
        }

        canvas.setForegroundColorRect(0, 0, s1, 1, stateColor);
        if (stateText.length() > s1) {
            canvas.type(0, 0, stateText.substring(0, s1));
            for (int dy=-1; dy > -4; dy--) canvas.setChar(s1 + dy, 0, '.');
        } else {
            canvas.typeCentered(0, 0, stateText, s1);
        }

        Address server = job.getNameserver();
        String label = server.label();
        if (label == null) {
            label = server.address();
        } else {
            label = server.address() + " (" + label + ")";
        }
        final int labelW = s2 - s1 - 1;
        if (label.length() > labelW) {
            canvas.type(s1 + 1, 0, label.substring(0, labelW));
            for (int dy=-1; dy > -4; dy--) canvas.setChar(s2 + dy, 0, '.');
        } else {
            canvas.typeCentered(s1 + 1, 0, label, labelW);
        }

        final int timeW = w - s2 - 1;
        switch (state) {
            case COMPLETE:
                long ns = job.getTime();
                double fns = (double) ns;
                String ms = TIMING_FORMAT.format(fns / 1e6) + " ms";
                double nsScale = Math.min(fns / 1e8d, 1d);

                int graphChars = timeW - ms.length() - 4;
                int fullChars = (int) Math.ceil((double) graphChars * nsScale);
                for (int dx=0; dx < graphChars; dx++) {
                    if (dx < fullChars) {
                        canvas.setChar(s2 + 2 + dx, 0, '█');
                        canvas.setForegroundColor(s2 + 2 + dx, 0, CharColor.CYAN_BRIGHT);
                    } else {
                        canvas.setChar(s2 + 2 + dx, 0, '─');
                        canvas.setForegroundColor(s2 + 2 + dx, 0, CharColor.BLACK);
                    }
                }

                canvas.type(w - ms.length() - 2, 0, ms);
                break;
            case ERROR:
                canvas.setForegroundColorRect(s2 + 1, 0, timeW, 1, CharColor.RED_BRIGHT);
                String errorMessage = job.getErrorMessage();
                if (errorMessage.length() > timeW) {
                    canvas.type(s2 + 1, 0, errorMessage.substring(0, timeW));
                    for (int dy=-1; dy > -4; dy--) canvas.setChar(w + dy, 0, '.');
                } else {
                    canvas.typeCentered(s2 + 1, 0, errorMessage, timeW);
                }
                break;
            default:
                canvas.setForegroundColorRect(s2 + 1, 0, timeW, 1, CharColor.BLACK);
                for (int dx=2; dx < timeW; dx++) {
                    canvas.setChar(s2 + dx, 0, '┅');
                }
                break;
        }

        /*
        Address server = job.getNameserver();
        String label = server.label();
        if (label == null) label = server.address();

        if (label.length() > s1) {
            canvas.type(0, 0, label.substring(0, s1));
            for (int dy=-1; dy > -4; dy--) canvas.setChar(s1 + dy, 0, '.');
        } else {
            canvas.typeCentered(0, 0, label, s1);
        }

         */
    }

    private void drawProgressBar(ColoredCharCanvas canvas) {
        final int w = canvas.getWidth();

        double progress = this.bench.getProgress();
        final String percentage = ((int) Math.floor(progress * 100d)) + "%";
        final int px = w - percentage.length();
        final int mw = px - 1;

        canvas.type(px, 0, percentage);

        int steps = mw * 4;
        int step = (int) Math.floor(progress * ((double) steps));
        int iChar = step >> 2;
        int bound = (steps == step ? iChar : iChar + 1);
        step &= 0b11;

        canvas.setForegroundColorRect(0, 0, mw, 1, CharColor.MAGENTA_BRIGHT);
        for (int i=0; i < bound; i++) {
            canvas.setChar(i, 0, PROGESS_STAGES[iChar == i ? step : 3]);
        }
    }

    @Override
    public boolean alwaysDraw() {
        return true;
    }

    @Override
    public void onInput(@NotNull TUI tui, char input) {
        switch (input) {
            case 'Q':
            case 'q':
                tui.setStage(new MainStage(this.context));
                break;
            case 'S':
            case 's':
                if (this.running) {
                    this.bench.stop();
                    this.running = false;
                } else {
                    this.bench.close();
                    this.bench = new JDNSBench(this.context.options());
                    this.bench.start();
                    this.running = true;
                }
                break;
            case 'T':
            case 't':
                this.selection = 0;
                if (this.sort.getCategory() == JDNSBenchJobSort.Category.STATE) {
                    this.sort = this.sort.reversed();
                } else {
                    this.sort = JDNSBenchJobSort.STATE;
                }
                break;
            case 'I':
            case 'i':
                this.selection = 0;
                if (this.sort.getCategory() == JDNSBenchJobSort.Category.LABEL) {
                    this.sort = this.sort.reversed();
                } else {
                    this.sort = JDNSBenchJobSort.LABEL;
                }
                break;
            case 'M':
            case 'm':
                this.selection = 0;
                if (this.sort.getCategory() == JDNSBenchJobSort.Category.TIME) {
                    this.sort = this.sort.reversed();
                } else {
                    this.sort = JDNSBenchJobSort.TIME;
                }
                break;
        }
    }

    @Override
    public void onInputArrowKey(@NotNull TUI tui, @NotNull TUIArrowKey key) {
        switch (key) {
            case UP:
                this.selection--;
                break;
            case DOWN:
                this.selection++;
                break;
        }
    }

}
