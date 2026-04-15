package org.mangorage.gamehub.snake;

import org.mangorage.jnwa.api.event.Event;
import org.mangorage.jnwa.api.window.Graphics;
import org.mangorage.jnwa.api.screen.Screen;
import org.mangorage.jnwa.api.window.Window;
import org.mangorage.jnwa.api.event.WindowKeyEvent;
import org.mangorage.gamehub.MainMenuScreen;

import java.awt.Color;
import java.awt.event.KeyEvent;

public class SnakeMainMenuScreen implements Screen {
    private Window window;
    private long startTime;

    @Override
    public void init(Window window) {
        this.window = window;
        this.startTime = System.currentTimeMillis();
        window.setTitle("Snake Game - Main Menu");
        window.setSize(1200, 800);
        window.setSizeLock(true);
    }

    @Override
    public void update() {
        // No logic needed for a static menu,
        // but you could add a moving background here later!
    }

    @Override
    public void render(Graphics g, Window window) {
        // Background
        g.clear(0xFF000000);
        g.setColor(0xFF1A1A1A);
        g.fillRect(0, 0, g.width(), g.height());

        int centerX = window.width() / 2;
        int centerY = window.height() / 2;

        // 1. Draw a decorative "Snake" using small rects
        g.setColor(Color.GREEN.getRGB());
        for (int i = 0; i < 5; i++) {
            g.fillRect(centerX - 125 + (i * 50), centerY - 150, 45, 45);
        }

        // 2. Main Title
        String title = "SNAKE NATIVE";
        int titleSize = 80;
        int titleW = g.getTextWidth(title, titleSize, true);
        g.setColor(Color.WHITE.getRGB());
        g.renderText(title, centerX - (titleW / 2), centerY - 50, titleSize, true);

        // 3. Flashing "Press SPACE" prompt
        // Uses sine wave based on time to create a "breathe" effect
        long elapsed = System.currentTimeMillis() - startTime;
        if ((elapsed / 500) % 2 == 0) {
            String prompt = "Press SPACE to Start";
            int promptSize = 30;
            int promptW = g.getTextWidth(prompt, promptSize, false);
            g.setColor(Color.YELLOW.getRGB());
            g.renderText(prompt, centerX - (promptW / 2), centerY + 50, promptSize, false);
        }

        // 4. Credits
        String credits = "Powered by Win32 & Java Panama";
        int creditsW = g.getTextWidth(credits, 15, false);
        g.setColor(0xFF888888);
        g.renderText(credits, centerX - (creditsW / 2), window.height() - 40, 15, false);
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof WindowKeyEvent(int keyCode, boolean pressed) && pressed) {
            // Start the game when Space is pressed
            if (keyCode == KeyEvent.VK_SPACE) {
                window.setScreen(new SnakeGameScreen());
            }
            if (keyCode == KeyEvent.VK_M) {
                window.setScreen(new MainMenuScreen());
            }
        }
    }

    @Override
    public void dispose() {
        // Cleanup if necessary
    }
}