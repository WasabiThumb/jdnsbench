package io.github.wasabithumb.jdnsbench.tui.stage.impl;

import io.github.wasabithumb.jdnsbench.api.address.Address;
import io.github.wasabithumb.jdnsbench.api.address.provider.AddressProvider;
import io.github.wasabithumb.jdnsbench.api.address.source.AddressSource;
import io.github.wasabithumb.jdnsbench.api.bench.JDNSBenchOptions;
import io.github.wasabithumb.jdnsbench.tui.TUI;
import io.github.wasabithumb.jdnsbench.tui.TUIArrowKey;
import io.github.wasabithumb.jdnsbench.tui.TUIContext;
import io.github.wasabithumb.jdnsbench.tui.stage.TUIStage;
import io.github.wasabithumb.jdnsbench.tui.bitmap.CharColor;
import io.github.wasabithumb.jdnsbench.tui.bitmap.ColoredCharCanvas;
import io.github.wasabithumb.jdnsbench.tui.stage.impl.config.ConfigStageModal;
import io.github.wasabithumb.jdnsbench.util.collections.FlipList;
import io.github.wasabithumb.jdnsbench.util.collections.MappingList;
import static io.github.wasabithumb.jdnsbench.util.AnsiUtil.*;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ConfigStage implements TUIStage {

    private static final String[] ACTION_TEXTS = new String[] {
            "Add IP Address",
            "Add IP Source",
            "Set Domain",
            "Set Repeats",
            "Set Timeout",
            "Set Delay",
            "Save & Exit"
    };
    private static final int MIN_WIDTH = 51;
    private static final int MIN_HEIGHT = 11;

    private final TUIContext context;
    @ApiStatus.Internal
    public List<AddressProvider> addresses;
    @ApiStatus.Internal
    public String domain;
    @ApiStatus.Internal
    public int reps;
    @ApiStatus.Internal
    public long timeout;
    @ApiStatus.Internal
    public long period;
    @ApiStatus.Internal
    public boolean useV6;

    private int selectionX = 0;
    private int selectionY = 0;
    private ConfigStageModal modal = null;

    public ConfigStage(TUIContext context) {
        this.context = context;
    }

    @Override
    public void onAttach(@NotNull TUI tui) {
        final JDNSBenchOptions opts = this.context.options();

        final Collection<AddressSource> a = opts.nameserverSources();
        final Collection<Address> b = opts.nameservers();
        List<AddressProvider> addresses = new ArrayList<>(a.size() + b.size());
        for (AddressSource ae : a) addresses.add(AddressProvider.of(ae));
        for (Address be : b) addresses.add(AddressProvider.of(be));

        this.addresses = addresses;
        this.domain = opts.domain();
        this.reps = opts.reps();
        this.timeout = opts.timeout();
        this.period = opts.period();
        this.useV6 = opts.useV6();
    }

    @Override
    public void onDetach(@NotNull TUI tui) {
        final int count = this.addresses.size();

        Object[] handles = new Object[count];
        int ah = 0;
        final int ibh = count - 1;
        int bh = ibh;

        Object handle;
        for (AddressProvider ae : this.addresses) {
            handle = ae.handle();
            if (ae.isSource()) {
                handles[ah++] = handle;
            } else {
                handles[bh--] = handle;
            }
        }

        List<Object> handleList = Arrays.asList(handles);
        List<Object> sources = (ah == 0) ? Collections.emptyList() : handleList.subList(0, ah);
        List<Object> addresses;
        if (bh == ibh) {
            addresses = Collections.emptyList();
        } else {
            addresses = handleList.subList(bh + 1, count);
            addresses = new FlipList<>(addresses);
        }

        this.context.options(JDNSBenchOptions.builder()
                .nameserverSources(new MappingList<>(sources, AddressSource.class))
                .nameservers(new MappingList<>(addresses, Address.class))
                .domain(this.domain)
                .reps(this.reps)
                .timeout(this.timeout)
                .period(this.period)
                .useV6(this.useV6)
                .build()
        );
    }

    @Override
    public void draw(@NotNull TUI tui) {
        final int w = tui.width();
        final int h = tui.height();
        if (w < MIN_WIDTH || h < MIN_HEIGHT) {
            System.out.print(tui.ansi().eraseScreen().cursor(0, 0));
            System.out.println(tui.ansi().fgBrightRed().a("Your terminal is too small!"));
            System.out.println(tui.ansi().fgBrightRed().a("Resize your terminal or press ESC to exit."));
            return;
        }

        ColoredCharCanvas canvas = new ColoredCharCanvas(w, h);
        int div = Math.min(Math.floorDiv(w, 3), 28);

        canvas.setRow(0, '═');
        canvas.setRow(h - 1, '═');
        canvas.setRow(2, '═');
        canvas.setColumn(0, '║');
        canvas.setColumn(w - 1, '║');
        canvas.setColumn(div, '║');
        canvas.setChar(0, 0, '╔');
        canvas.setChar(w - 1, 0, '╗');
        canvas.setChar(0, h - 1, '╚');
        canvas.setChar(w - 1, h - 1, '╝');
        canvas.setChar(div, 0, '╦');
        canvas.setChar(div, 2, '╬');
        canvas.setChar(div, h - 1, '╩');
        canvas.setChar(0, 2, '╠');
        canvas.setChar(w - 1, 2, '╣');

        canvas.typeCentered(1, 1, "ACTIONS", div - 1);
        canvas.setForegroundColorRect(1, 1, div - 1, 1, CharColor.CYAN_BRIGHT);
        canvas.typeCentered(div + 1, 1, "NAMESERVERS", w - div - 2);
        canvas.setForegroundColorRect(div + 1, 1, w - div - 2, 1, CharColor.CYAN_BRIGHT);

        this.drawActions(canvas.subCanvas(2, 3, div - 3, h - 4), this.selectionX == 0);
        this.drawServers(canvas.subCanvas(div, 2, w - div, h - 2), this.selectionX == 1);

        final boolean hasModal = this.modal != null;
        if (hasModal) System.out.print(ESC_DIM);
        canvas.print();
        if (hasModal) {
            System.out.print(ESC_DIM_OFF);
            this.modal.draw(tui, this, w, h);
        }
    }

    private void drawServers(@NotNull ColoredCharCanvas canvas, boolean selected) {
        final int w = canvas.getWidth();
        final int h = canvas.getHeight();
        int s1 = Math.floorDiv(w, 2);

        canvas.setColumn(s1, '│');
        canvas.setChar(s1, 0, '╤');
        canvas.setChar(s1, h - 1, '╧');

        canvas.setRow(2, '─');
        canvas.setChar(0, 2, '╟');
        canvas.setChar(w - 1, 2, '╢');
        canvas.setChar(s1, 2, '┼');

        this.drawRow(canvas.subCanvas(0, 1, w, 1), false, s1, "INFO", "LABEL");

        final int total = this.addresses.size();
        if (total == 0) return;

        int y = 3;
        final int capacity = h - y - 1;
        int selection;
        if (selected) {
            selection = this.selectionY;
            if (selection < 0) {
                this.selectionY = selection = (total - 1);
            } else if (selection >= total) {
                this.selectionY = selection = 0;
            }
        } else {
            selection = 0;
        }
        int page = Math.floorDiv(selection, capacity);
        int cur = page * capacity;

        AddressProvider entry;
        for (int i=0; i < capacity; i++) {
            if (cur >= total) break;
            entry = this.addresses.get(cur);

            String info;
            if (entry.isSource()) {
                String hex = Integer.toUnsignedString(entry.handle().hashCode(), 16).toUpperCase(Locale.ROOT);
                int pad = 8 - hex.length();
                if (pad > 0) hex = "0".repeat(pad) + hex;
                info = "SOURCE[0x" + hex + "]";
            } else {
                info = ((Address) entry.handle()).address();
            }

            this.drawRow(
                    canvas.subCanvas(0, y, w, 1),
                    selected && (cur == selection),
                    s1,
                    info,
                    entry.label()
            );

            y++;
            cur++;
        }
    }

    private void drawRow(@NotNull ColoredCharCanvas canvas, boolean selected, int s1, String a, String b) {
        final int w = canvas.getWidth();
        final int x1 = 1;
        final int x2 = s1 + 1;
        final int w1 = s1 - 1;
        final int w2 = w - s1 - 2;
        if (selected) {
            canvas.setBackgroundColorRect(x1, 0, w1, 1, CharColor.RED_BRIGHT);
            canvas.setBackgroundColorRect(x2, 0, w2, 1, CharColor.RED_BRIGHT);
            canvas.setBackgroundColorRect(s1, 0, 1, 1, CharColor.RED);
        }
        if (a.length() > w1) {
            canvas.type(x1, 0, a.substring(0, w1));
            for (int i=1; i < 4; i++) canvas.setChar(s1 - i, 0, '.');
        } else {
            canvas.typeCentered(x1, 0, a, w1);
        }
        if (b.length() > w2) {
            canvas.type(x2, 0, b.substring(0, w2));
            for (int i=2; i < 5; i++) canvas.setChar(w - i, 0, '.');
        } else {
            canvas.typeCentered(x2, 0, b, w2);
        }
    }

    private void drawActions(@NotNull ColoredCharCanvas canvas, boolean selected) {
        int ySpan = ACTION_TEXTS.length;
        if (selected) {
            if (this.selectionY < 0) {
                this.selectionY = ySpan - 1;
            } else if (this.selectionY >= ySpan) {
                this.selectionY = 0;
            }
        }

        int ySpan2 = ySpan * 2;
        boolean pad = false;
        if (canvas.getHeight() > ySpan2) {
            ySpan = ySpan2;
            pad = true;
        }

        int y = Math.floorDiv(canvas.getHeight() - ySpan, 2);
        for (int i=0; i < ACTION_TEXTS.length; i++) {
            if (selected && i == this.selectionY) {
                canvas.setBackgroundColorRect(
                        0, y,
                        canvas.getWidth(), 1,
                        CharColor.GREEN
                );
                canvas.setForegroundColorRect(
                        0, y,
                        canvas.getWidth(), 1,
                        CharColor.BLACK
                );
            }
            canvas.typeCentered(0, y++, ACTION_TEXTS[i], canvas.getWidth());
            if (pad) y++;
        }
    }

    @Override
    public void onInput(@NotNull TUI tui, char input) {
        if (this.modal != null) {
            if (this.modal.onInput(tui, this, input)) this.modal = this.modal.getNext();
            return;
        }
        if (input == ((char) 32) || input == ((char) 13) || input == ((char) 10)) {
            // space or enter
            if (this.selectionX == 0) {
                // actions
                switch (this.selectionY) {
                    case 0:
                        this.modal = ConfigStageModal.addIPAddress();
                        break;
                    case 1:
                        this.modal = ConfigStageModal.addIPSource();
                        break;
                    case 2:
                        this.modal = ConfigStageModal.setDomain(this.domain);
                        break;
                    case 3:
                        this.modal = ConfigStageModal.setRepeats(this.reps);
                        break;
                    case 4:
                        this.modal = ConfigStageModal.setTimeout(this.timeout);
                        break;
                    case 5:
                        this.modal = ConfigStageModal.setDelay(this.period);
                        break;
                    case 6:
                        // Save & Exit
                        tui.setStage(new MainStage(this.context));
                        break;
                }
            } else if (this.selectionX == 1) {
                int index = this.selectionY;
                int size = this.addresses.size();
                if (index < 0) return;
                if (index >= size) return;
                this.addresses.remove(index);
                if (index == (size - 1)) this.selectionY--;
            }
        }
    }

    @Override
    public void onInputArrowKey(@NotNull TUI tui, @NotNull TUIArrowKey key) {
        if (this.modal != null) {
            this.modal.onInputArrowKey(tui, this, key);
            return;
        }
        switch (key) {
            case LEFT:
            case RIGHT:
                this.selectionX = (this.selectionX == 0) ? 1 : 0;
                this.selectionY = 0;
                break;
            case DOWN:
                this.selectionY++;
                break;
            case UP:
                this.selectionY--;
                break;
        }
    }

}
