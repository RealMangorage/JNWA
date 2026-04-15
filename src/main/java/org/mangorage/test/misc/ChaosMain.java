package org.mangorage.test.misc;

import org.mangorage.nsapi.api.ScreenAPI;
import org.mangorage.nsapi.api.Window;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


import static org.mangorage.test.Test.getInternalIcon;

public class ChaosMain {

    public static void main(String[] args) throws InterruptedException {
        ScreenAPI api = ScreenAPI.of();
        TestScreen screen = new TestScreen(null);

        // ---- Get total virtual desktop bounds ----
        Rectangle virtualBounds = getVirtualScreenBounds();
        System.out.println("Virtual desktop: " + virtualBounds);

        int windowCount = 10;
        List<Window> windows = new ArrayList<>();
        Random random = new Random();

        for (int i = 0; i < windowCount; i++) {
            Window window = api.createWindow(
                    "win_" + i,
                    "Chaos " + i
            );

            var icon = getInternalIcon("IconExample.png");
            if (icon != null) {
                window.setIcon(icon);
            }

            window.show();
            window.setScreen(screen);
            window.start();

            windows.add(window);

            // Each window gets its own chaos thread
            Thread mover = new Thread(() -> {
                try {
                    while (true) {
                        int x = virtualBounds.x + random.nextInt(virtualBounds.width);
                        int y = virtualBounds.y + random.nextInt(virtualBounds.height);

                        window.setPosition(x, y);

                        Thread.sleep(30 + random.nextInt(70));
                    }
                } catch (InterruptedException ignored) {}
            });

            mover.setDaemon(true);
            mover.start();
        }

        // keep alive
        Thread.sleep(60_000); // 1 min
        System.exit(0);
    }

    // ---- Computes full multi-monitor bounds ----
    private static Rectangle getVirtualScreenBounds() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] devices = new GraphicsDevice[]{ge.getDefaultScreenDevice()};

        Rectangle virtual = new Rectangle();

        for (GraphicsDevice device : devices) {
            GraphicsConfiguration gc = device.getDefaultConfiguration();
            Rectangle bounds = gc.getBounds();
            virtual = virtual.union(bounds);
        }

        return virtual;
    }
}
