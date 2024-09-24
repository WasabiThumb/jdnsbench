package io.github.wasabithumb.jdnsbench;

import io.github.wasabithumb.jdnsbench.jni.JNISystemLibraries;
import io.github.wasabithumb.jdnsbench.tui.TUI;
import io.github.wasabithumb.jdnsbench.tui.stage.TUIStage;
import io.github.wasabithumb.jdnsbench.tui.stage.impl.MainStage;
import io.github.wasabithumb.jdnsbench.tui.stage.impl.MissingLibraryStage;
import io.github.wasabithumb.jdnsbench.util.ReflectUtil;
import io.github.wasabithumb.jdnsbench.util.SystemUtil;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class Main {

    public static void main(String[] args) {
        if (SystemUtil.IS_WINDOWS) {
            if (SystemUtil.WINDOWS_MAJOR_VERSION < 8) {
                fatal("This program requires Windows 8+");
                return;
            }
        } else if (!SystemUtil.IS_LINUX) {
            fatal("This program can only run on Windows or Linux");
            return;
        }
        terminalCheck(args);

        Terminal term;
        try {
            term = TerminalBuilder.builder().build();
        } catch (IOException e) {
            fatal("Terminal not supported");
            return;
        }
        term.enterRawMode();

        try (TUI tui = new TUI(term)) {
            if (JNISystemLibraries.missingCares()) {
                tui.setStage(getMissingCaresStage());
            } else {
                tui.setStage(new MainStage());
            }
            tui.join();
        } catch (IOException ignored) { }
    }

    private static void terminalCheck(String[] args) {
        if (args.length != 0 && Arrays.asList(args).contains("--no-terminal-check")) return;
        if (System.console() == null && !GraphicsEnvironment.isHeadless()) {
            String jar = ReflectUtil.getCodeSource().getAbsolutePath();
            String sep = SystemUtil.IS_WINDOWS ? "\"" : "";
            String javaCmd = "java -jar " + sep + jar + sep + " --no-terminal-check";
            String cmd;
            if (SystemUtil.IS_WINDOWS) {
                cmd = "cmd /c start cmd /k " + javaCmd;
            } else {
                cmd = "gnome-terminal -- " + javaCmd;
            }
            try {
                Process p = Runtime.getRuntime().exec(cmd.split(" "));
                while (p.isAlive()) TimeUnit.MILLISECONDS.sleep(10);
                System.exit(p.exitValue());
                return;
            } catch (Exception e) {
                String msg;
                try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
                     OutputStreamWriter raw = new OutputStreamWriter(bos, StandardCharsets.UTF_8);
                     PrintWriter pw = new PrintWriter(raw)
                ) {
                    e.printStackTrace(pw);
                    pw.flush();
                    msg = bos.toString(StandardCharsets.UTF_8);
                } catch (IOException e1) {
                    msg = e1.getMessage();
                }
                JOptionPane.showMessageDialog(null, msg, "jDNSBench", JOptionPane.ERROR_MESSAGE);
            }
            JOptionPane.showMessageDialog(null, "You must run this program in a terminal!", "jDNSBench", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private static void fatal(final String message) {
        System.err.println(message);
        System.exit(1);
    }

    private static TUIStage getMissingCaresStage() {
        MissingLibraryStage stage = new MissingLibraryStage("C-ARES");
        stage.addInstructions(
                "Alpine Linux",
                "apk update",
                "apk add --upgrade c-ares"
        );
        stage.addInstructions(
                "Arch Linux",
                "# Make sure the \"extra\" repository is enabled in /etc/pacman.conf",
                "pacman -Syu c-ares"
        );
        stage.addInstructions(
                "CentOS",
                "dnf install c-ares"
        );
        stage.addInstructions(
                "Debian",
                "sudo apt-get update",
                "sudo apt-get install libc-ares2"
        );
        stage.addInstructions(
                "Fedora (RHEL)",
                "dnf install c-ares"
        );
        stage.addInstructions(
                "FreeBSD",
                "pkg install c-ares"
        );
        stage.addInstructions(
                "OpenSUSE",
                "zypper install c-ares-utils"
        );
        stage.addInstructions(
                "Oracle Linux",
                "dnf install c-ares"
        );
        stage.addInstructions(
                "Rocky Linux",
                "dnf install c-ares"
        );
        stage.addInstructions(
                "Solus",
                "sudo eopkg install c-ares"
        );
        stage.addInstructions(
                "Ubuntu",
                "sudo apt-get update",
                "sudo apt-get install libc-ares2"
        );
        stage.addInstructions(
                "Void Linux",
                "xbps-install -Su c-ares"
        );
        return stage;
    }

}
