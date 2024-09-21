package io.github.wasabithumb.jdnsbench.tui;

import io.github.wasabithumb.jdnsbench.tui.stage.TUIStage;
import org.jetbrains.annotations.NotNull;
import org.jline.jansi.Ansi;
import org.jline.terminal.Terminal;

import java.io.Closeable;
import java.io.IOException;

public class TUI implements Closeable {

    private final Terminal terminal;
    private final TUIWorker worker;
    public TUI(final Terminal terminal) {
        this.terminal = terminal;
        this.worker = new TUIWorker(this);
        this.worker.start();
    }

    public final @NotNull Terminal terminal() {
        return this.terminal;
    }

    public final @NotNull Ansi ansi() {
        return Ansi.ansi(this.terminal.getWidth());
    }

    public final int width() {
        return this.terminal.getWidth();
    }

    public final int height() {
        return this.terminal.getHeight();
    }

    public void setStage(final TUIStage stage) {
        this.worker.setStage(stage);
    }

    public void join() {
        try {
            this.worker.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void shutdown() {
        this.worker.shutdown();
    }

    @Override
    public void close() throws IOException {
        this.shutdown();
        this.terminal.close();
    }

}
