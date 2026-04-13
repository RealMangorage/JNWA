package org.mangorage.test;

import org.mangorage.nsapi.api.ScreenAPI;
import org.mangorage.nsapi.api.Window;

import java.io.IOException;

public final class Test {


    public static void main(String[] args) throws InterruptedException, IOException {
        ScreenAPI api = ScreenAPI.of();

        var screen = new MainMenuScreen();

        Window window = api.createWindow("win_1", "Test", 800, 600);

        final var icon = getInternalIcon("IconExample.png");
        if (icon != null) {
            window.setIcon(icon);
        }

        window.show();
        window.setScreen(screen);
        window.start();
    }

    public static byte[] getInternalIcon(String path) {
        try (final var IS = Thread.currentThread().getContextClassLoader().getResourceAsStream(path)) {
            return IS == null ? null : IS.readAllBytes();
        } catch (IOException e) {
            return null;
        }
    }
}