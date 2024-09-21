package io.github.wasabithumb.jdnsbench.gui;

import io.github.wasabithumb.jdnsbench.api.JDNS;
import io.github.wasabithumb.jdnsbench.gui.asset.UIAssets;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.ThreadLocalRandom;

public class MainWindow extends JFrame {

    private final JDNS runtime = new JDNS();

    public MainWindow(UIAssets assets) {
        super("jDNSBench");
        this.setIconImage(assets.getIcon());
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setupContent(this.getContentPane());
        this.pack();
    }

    private void setupContent(Container pane) {
        pane.setLayout(new GridLayout(1, 1));
        pane.add(new Canvas());
    }

    public void open() {
        this.setSize(640, 480);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    @Override
    public void dispose() {
        super.dispose();
        this.runtime.close();
        System.exit(0);
    }

    private static class Canvas extends JPanel {

        @Override
        protected void paintComponent(Graphics g) {
            g.setColor(new Color(ThreadLocalRandom.current().nextInt(0x1000000)));
            g.fillRect(0, 0, this.getWidth(), this.getHeight());
        }

    }

}
