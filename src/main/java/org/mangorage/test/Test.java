package org.mangorage.test;

import org.mangorage.jnwapi.api.ScreenAPI;
import org.mangorage.jnwapi.api.Window;

import java.io.IOException;

public final class Test {


    public static void main(String[] args) throws InterruptedException, IOException {
        ScreenAPI api = ScreenAPI.of();

        for (int i = 0; i < 2; i++) {
            var screen = new MainMenuScreen();

            Window window = api.createWindow("win_" + i, "Test");
            window.setPosition(100, 100);

            final var icon = getInternalIcon("IconExample.png");
            if (icon != null) {
                window.setIcon(icon);
            }

            window.show();
            window.setScreen(screen);
            window.start();
        }
    }

    public static byte[] getInternalIcon(String path) {
        try (final var IS = Thread.currentThread().getContextClassLoader().getResourceAsStream(path)) {
            return IS == null ? null : IS.readAllBytes();
        } catch (IOException e) {
            return null;
        }
    }
}