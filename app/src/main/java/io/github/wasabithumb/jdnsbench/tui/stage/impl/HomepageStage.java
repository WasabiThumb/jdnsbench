package io.github.wasabithumb.jdnsbench.tui.stage.impl;

import io.github.wasabithumb.jdnsbench.tui.TUI;
import io.github.wasabithumb.jdnsbench.tui.stage.TUIStage;
import io.github.wasabithumb.jdnsbench.util.SystemUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Random;

public class HomepageStage implements TUIStage {

    private static final String HOMEPAGE;
    private static final URI HOMEPAGE_URI;
    static {
        HOMEPAGE = "https://github.com/WasabiThumb/jdnsbench/";
        try {
            HOMEPAGE_URI = new URI(HOMEPAGE);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private GayBallJail jail;
    public HomepageStage() { }

    @Override
    public void onAttach(@NotNull TUI tui) {
        this.jail = new GayBallJail();

        // Try opening the homepage in browser
        // Method 1: AWT
        Desktop desktop;
        if (Desktop.isDesktopSupported() &&
                (desktop = Desktop.getDesktop()) != null &&
                desktop.isSupported(Desktop.Action.BROWSE)
        ) {
            try {
                desktop.browse(HOMEPAGE_URI);
                return;
            } catch (IOException ignored) { }
        }

        String cmd;
        if (SystemUtil.IS_WINDOWS) {
            // Method 2: WINDOWS
            cmd = "rundll32 url.dll,FileProtocolHandler " + HOMEPAGE;
        } else {
            // Method 3: UNIX
            cmd = "xdg-open " + HOMEPAGE;
        }

        try {
            Runtime.getRuntime().exec(cmd);
        } catch (IOException ignored) { }
    }

    @Override
    public boolean alwaysDraw() {
        return true;
    }

    @Override
    public void draw(@NotNull TUI tui) {
        final int w = tui.width();
        final int h = tui.height();
        char[] line = new char[w];

        int px = 0;
        int py = 0;
        int dim = w;
        if (w != h) {
            if (w < h) {
                dim = h;
                px = Math.floorDiv(h - w, 2);
            } else {
                py = Math.floorDiv(w - h, 2);
            }
        }

        int v;
        for (int y=0; y < h; y++) {
            if (y != 0) System.out.print('\n');
            for (int x=0; x < w; x++) {
                v = this.jail.sample4(x + px, y + py, dim, dim);
                line[x] = switch (v) {
                    case 0 -> ' ';
                    case 1 -> '░';
                    case 2 -> '▒';
                    case 3 -> '▓';
                    case 4 -> '█';
                    default -> throw new IllegalStateException("Unexpected value: " + v);
                };
            }
            System.out.print(tui.ansi().fgRgb(0x7f7f7f).a(line).reset());
        }

        this.jail.step();
    }

    //

    private static class GayBallJail {

        private static final int BALL_COUNT = 12;
        private static final double BALL_MIN_RADIUS = 0.05;
        private static final double BALL_MAX_RADIUS = 0.1;
        private static final double BALL_MIN_SPEED = 0.025;
        private static final double BALL_MAX_SPEED = 0.05;

        private final double[] ballX  = new double[BALL_COUNT];
        private final double[] ballY  = new double[BALL_COUNT];
        private final double[] ballR  = new double[BALL_COUNT];
        private final double[] ballDX = new double[BALL_COUNT];
        private final double[] ballDY = new double[BALL_COUNT];

        GayBallJail() {
            Random random = new Random();
            for (int i = 0; i < BALL_COUNT; i++) this.createBall(random, i);
        }

        private void createBall(@NotNull Random random, int index) {
            final double radius = BALL_MIN_RADIUS + (random.nextDouble() * (BALL_MAX_RADIUS - BALL_MIN_RADIUS));
            final double range = 1d - radius - radius;
            final double speed = BALL_MIN_SPEED + (random.nextDouble() * (BALL_MAX_SPEED - BALL_MIN_SPEED));
            final double angle = random.nextDouble() * Math.PI * 2d;

            this.ballX[index] = random.nextDouble() * range + radius;
            this.ballY[index] = random.nextDouble() * range + radius;
            this.ballR[index] = radius;
            this.ballDX[index] = speed * Math.cos(angle);
            this.ballDY[index] = speed * Math.sin(angle);
        }

        void step() {
            for (int i = 0; i < BALL_COUNT; i++) this.stepBall(i);
        }

        private void stepBall(int index) {
            double x = this.ballX[index];
            double y = this.ballY[index];
            double dx = this.ballDX[index];
            double dy = this.ballDY[index];

            x += dx;
            if (x < 0) {
                x = 0;
                dx = -dx;
            } else if (x > 1) {
                x = 1;
                dx = -dx;
            }

            y += dy;
            if (y < 0) {
                y = 0;
                dy = -dy;
            } else if (y > 1) {
                y = 1;
                dy = -dy;
            }

            this.ballX[index] = x;
            this.ballY[index] = y;
            this.ballDX[index] = dx;
            this.ballDY[index] = dy;
        }

        @SuppressWarnings("UnnecessaryLocalVariable")
        @Range(from=0L, to=4L) int sample4(int x, int y, int w, int h) {
            final double dw = w;
            final double dh = h;
            final double x1 = ((double) x) / dw;
            final double x2 = ((double) (x + 1)) / dw;
            final double y1 = ((double) y) / dh;
            final double y2 = ((double) (y + 1)) / dh;
            int ret = 0;

            if (this.sample(x1, y1)) ret++;
            if (this.sample(x2, y1)) ret++;
            if (this.sample(x1, y2)) ret++;
            if (this.sample(x2, y2)) ret++;

            return ret;
        }

        boolean sample(double x, double y) {
            boolean ret = false;
            for (int i = 0; i < BALL_COUNT; i++) {
                if (this.sampleBall(x, y, i)) ret = !ret;
            }
            return ret;
        }

        private boolean sampleBall(double x, double y, int index) {
            double distSqr = Math.pow((x - this.ballX[index]) / 2d, 2d) + Math.pow(y - this.ballY[index], 2d);
            double radSqr = Math.pow(this.ballR[index], 2d);
            return distSqr <= radSqr;
        }

    }

}
