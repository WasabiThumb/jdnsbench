package io.github.wasabithumb.jdnsbench.tui;

import io.github.wasabithumb.jdnsbench.tui.stage.TUIStage;
import static io.github.wasabithumb.jdnsbench.util.AnsiUtil.*;

import io.github.wasabithumb.jdnsbench.util.SystemUtil;
import org.jetbrains.annotations.Nullable;
import org.jline.jansi.Ansi;
import org.jline.utils.NonBlockingReader;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

class TUIWorker extends Thread {

    private static final long MIN_DELTA = 25000000L;

    private final TUI tui;
    private boolean running = false;
    private boolean scheduledRepaint = true;
    private TUIStage activeStage = null;
    private TUIStage queuedStage = null;
    private boolean flipQueue = false;
    public TUIWorker(TUI TUI) {
        super("TUI Worker Thread");
        this.tui = TUI;
    }

    public void setStage(TUIStage stage) {
        synchronized (this) {
            this.queuedStage = stage;
            this.flipQueue = true;
        }
    }

    @Override
    public void start() {
        synchronized (this) {
            this.running = true;
            this.scheduledRepaint = true;
        }
        super.start();
    }

    @Override
    public void run() {
        System.out.print(this.tui.ansi().eraseScreen().saveCursorPosition());
        int lastWidth = this.tui.width();
        int lastHeight = this.tui.height();

        System.out.print(ESC_ENABLE_BUF);
        System.out.print(ESC_CURSOR_INVISIBLE);
        while (true) {
            final long start = System.nanoTime();
            TUIStage stage;
            synchronized (this) {
                if (!this.running) break;
                stage = this.activeStage;
                TUIStage queued = this.queuedStage;
                if (this.flipQueue) {
                    if (stage != null) stage.onDetach(this.tui);
                    if (queued != null) queued.onAttach(this.tui);
                    this.scheduledRepaint = true;
                    stage = this.activeStage = queued;
                    this.queuedStage = null;
                    this.flipQueue = false;
                }

                int thisWidth = this.tui.width();
                int thisHeight = this.tui.height();
                if (thisWidth != lastWidth || thisHeight != lastHeight) {
                    this.scheduledRepaint = true;
                    lastWidth = thisWidth;
                    lastHeight = thisHeight;
                }
            }

            this.handleInput(stage);
            this.handleRender(stage);

            final long end = System.nanoTime();
            final long elapsed = end - start;
            if (elapsed < MIN_DELTA) {
                try {
                    TimeUnit.NANOSECONDS.sleep(MIN_DELTA - elapsed);
                } catch (InterruptedException ignored) {
                    break;
                }
            }
        }
        System.out.print(ESC_CURSOR_VISIBLE);
        System.out.print(ESC_DISABLE_BUF);
        System.out.print(Ansi.ansi().eraseScreen().cursor(0, 0));
    }

    private void handleInput(@Nullable TUIStage stage) {
        NonBlockingReader reader = this.tui.terminal().reader();
        int read;
        char c;
        int escHead = -1;
        while (true) {
            try {
                read = reader.read(SystemUtil.IS_WINDOWS ? 10L : 1L);
            } catch (IOException ignored) {
                break;
            }
            if (read < 0) break;
            c = (char) read;

            if (escHead != -1) {
                if (escHead == 0) {
                    if (c != '[' && c != 'O') break;
                    escHead++;
                    continue;
                } else if (escHead == 1) {
                    TUIArrowKey key = TUIArrowKey.of(c);
                    if (key != TUIArrowKey.INVALID) {
                        if (stage != null) {
                            stage.onInputArrowKey(this.tui, key);
                            synchronized (this) {
                                this.scheduledRepaint = true;
                            }
                        }
                        escHead = -1;
                        continue;
                    } else {
                        escHead = 0;
                    }
                }
            }

            if (read == 27) { // ESC
                escHead = 0;
            } else if (stage != null) {
                stage.onInput(this.tui, c);
                synchronized (this) {
                    this.scheduledRepaint = true;
                }
            }
        }
        if (escHead == 0) { // pressed ESCAPE
            System.out.print(this.tui.ansi().eraseScreen());
            this.shutdown();
        }
    }

    private void handleRender(@Nullable TUIStage stage) {
        //
        if (stage != null) {
            if (!stage.alwaysDraw()) {
                synchronized (this) {
                    if (!this.scheduledRepaint) return;
                    this.scheduledRepaint = false;
                }
            }
            System.out.print(this.tui.ansi().reset().cursor(0, 0).cursorUp(this.tui.height()));
            stage.draw(this.tui);
        }
    }

    public void shutdown() {
        synchronized (this) {
            if (!this.running) return;
            this.running = false;
            TUIStage stage = this.activeStage;
            if (stage != null) stage.onDetach(this.tui);
        }
        this.interrupt();
    }

}
