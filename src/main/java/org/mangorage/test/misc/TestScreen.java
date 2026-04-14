package org.mangorage.test.misc;

import org.mangorage.nsapi.api.Graphics;
import org.mangorage.nsapi.api.Screen;
import org.mangorage.nsapi.api.Window;
import org.mangorage.nsapi.api.Event;
import org.mangorage.nsapi.api.event.MouseMoveEvent;
import org.mangorage.nsapi.api.event.WindowKeyEvent;

import java.awt.*;
import java.awt.event.KeyEvent;

public class TestScreen implements Screen {

    private final Image image;
    private int mouseX, mouseY, size = 0;
    private volatile Window window;

    public TestScreen(Image image) {
        this.image = image;
    }

    @Override
    public void init(Window window) {
        this.window = window;
    }

    private final long ms = System.currentTimeMillis();

    @Override
    public void update() {
        window.setTitle("Update: " + (System.currentTimeMillis() - ms));
    }


    @Override
    public void render(Graphics g, Window window) {
        g.clear(0xFF000000); // Clear to Black

        if (window.id().contains("1")) {
            g.setColor(
                    Color.BLUE.getRGB()
            );
        } else {
            g.setColor(
                    Color.GRAY.getRGB()
            );
        }
        g.fillRect(0, 0, g.width() + 5, g.height() + 5);
        g.setColor(
                Color.ORANGE.getRGB()
        );

        g.renderText("Hello, World!", 10, 10, 10, false);

        g.fillRect(mouseX, mouseY, size, size);

        if (image != null) {
            g.drawImage(image, 20, 20, 50, 50);
        }
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof MouseMoveEvent(int x, int y)) {
            this.mouseX = x;
            this.mouseY = y;
        }
        if (event instanceof WindowKeyEvent(int keyCode, boolean pressed)) {
            if (pressed) {
                if (keyCode == KeyEvent.VK_E) {
                    size++;
                } else if (keyCode == KeyEvent.VK_R) {
                    size--;
                }
            }
        }
    }

    @Override
    public void dispose() {
        System.out.println("Cleanup");
    }
}
