package org.mangorage.gamehub;

import org.mangorage.jnwa.api.window.WindowAPI;
import org.mangorage.jnwa.api.window.Window;

import java.io.IOException;

public final class GameHub {


    public static void main(String[] args) throws InterruptedException, IOException {
        WindowAPI api = WindowAPI.of();

        for (int i = 0; i < 1; i++) {
            var screen = new MainMenuScreen();

            Window window = api.createWindow("win_" + i, "GameHub");
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