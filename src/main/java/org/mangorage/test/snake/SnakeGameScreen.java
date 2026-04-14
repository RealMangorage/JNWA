package org.mangorage.test.snake;

import org.mangorage.nsapi.api.Graphics;
import org.mangorage.nsapi.api.Screen;
import org.mangorage.nsapi.api.Window;
import org.mangorage.nsapi.api.Event;
import org.mangorage.nsapi.api.event.WindowKeyEvent;
import org.mangorage.test.MainMenuScreen;

import java.awt.*;
import java.util.LinkedList;
import java.util.Random;

public final class SnakeGameScreen implements Screen {

    private static final int CELL = 50;

    private final LinkedList<Point> snake = new LinkedList<>();
    private Point food;
    private int dx = 1, dy = 0;

    private Window window;
    private final Random random = new Random();

    private long lastMove = 0;
    private long moveDelay = 120;

    private boolean dead = false;

    @Override
    public void init(Window window) {
        this.window = window;
        window.setTitle("Snake Game");
        reset();
    }

    private void reset() {
        dx = 1;
        dy = 0;
        dead = false;
        snake.clear();
        snake.add(new Point(5, 5));
        snake.add(new Point(4, 5));
        snake.add(new Point(3, 5));
        spawnFood();
    }

    @Override
    public void update() {
        if (dead) return;

        long now = System.currentTimeMillis();
        if (now - lastMove < moveDelay) return;
        lastMove = now;

        Point head = snake.getFirst();
        Point newHead = new Point(head.x + dx, head.y + dy);

        int maxX = window.width() / CELL;
        int maxY = window.height() / CELL;

        // Wall Collision
        if (newHead.x < 0 || newHead.y < 0 || newHead.x >= maxX || newHead.y >= maxY) {
            dead = true;
            return;
        }

        // Self Collision
        for (Point p : snake) {
            if (p.equals(newHead)) {
                dead = true;
                return;
            }
        }

        snake.addFirst(newHead);

        if (newHead.equals(food)) {
            spawnFood();
        } else {
            snake.removeLast();
        }

        window.setTitle("Snake Game -> Score: " + snake.size());
    }

    @Override
    public void render(Graphics g, Window window) {
        g.clear(0xFF000000);
        g.setColor(dead ? 0xFF330000 : 0xFF1A1A1A);
        g.fillRect(0, 0, g.width(), g.height());

        // Draw Snake
        g.setColor(Color.GREEN.getRGB());
        for (Point p : snake) g.fillRect(p.x * CELL, p.y * CELL, CELL, CELL);

        // Draw Food
        g.setColor(Color.RED.getRGB());
        if (food != null) g.fillRect(food.x * CELL, food.y * CELL, CELL, CELL);

        int centerX = window.width() / 2;
        int centerY = window.height() / 2;

        if (dead) {
            // 1. "Game Over!"
            String title = "Game Over!";
            int titleSize = 60;
            int titleW = g.getTextWidth(title, titleSize, true);
            g.setColor(Color.RED.getRGB());
            g.renderText(title, centerX - (titleW / 2), centerY - 80, titleSize, true);

            // 2. Restart Hint
            String hint = "Press R to restart";
            int hintW = g.getTextWidth(hint, 25, false);
            g.setColor(Color.WHITE.getRGB());
            g.renderText(hint, centerX - (hintW / 2), centerY, 25, false);

            // 3. Menu Hint (The new part)
            String menuHint = "Press M to go back to Menu";
            int menuW = g.getTextWidth(menuHint, 25, false);
            g.setColor(Color.YELLOW.getRGB());
            g.renderText(menuHint, centerX - (menuW / 2), centerY + 40, 25, false);

            // 4. Score
            String score = "Final Score: " + snake.size();
            int scoreW = g.getTextWidth(score, 20, false);
            g.setColor(Color.LIGHT_GRAY.getRGB());
            g.renderText(score, centerX - (scoreW / 2), centerY + 100, 20, false);
        } else {
            // HUD
            g.setColor(Color.WHITE.getRGB());
            g.renderText("Score: " + snake.size(), 20, 20, 20, false);
            g.setColor(0x88FFFFFF); // Faded white
            g.renderText("[M] Main Menu", 20, 50, 18, false);
        }
    }

    @Override
    public void onEvent(Event event) {
        if (!(event instanceof WindowKeyEvent ke)) return;
        if (!ke.pressed()) return;

        int keyCode = ke.keyCode();

        switch (keyCode) {
            case java.awt.event.KeyEvent.VK_W -> {
                if (dy == 1) return;
                dx = 0; dy = -1;
            }
            case java.awt.event.KeyEvent.VK_S -> {
                if (dy == -1) return;
                dx = 0; dy = 1;
            }
            case java.awt.event.KeyEvent.VK_A -> {
                if (dx == 1) return;
                dx = -1; dy = 0;
            }
            case java.awt.event.KeyEvent.VK_D -> {
                if (dx == -1) return;
                dx = 1; dy = 0;
            }
            case java.awt.event.KeyEvent.VK_R -> {
                reset();
            }
            case java.awt.event.KeyEvent.VK_M -> {
                // Return to Main Menu anytime
                window.setScreen(new MainMenuScreen());
            }
        }
    }

    @Override
    public void dispose() {
    }

    private void spawnFood() {
        int maxX = window.width() / CELL;
        int maxY = window.height() / CELL;
        if (maxX <= 0 || maxY <= 0) return;
        food = new Point(random.nextInt(maxX), random.nextInt(maxY));
    }
}