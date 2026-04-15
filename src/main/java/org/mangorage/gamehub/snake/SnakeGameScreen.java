package org.mangorage.gamehub.snake;

import org.mangorage.jnwa.api.window.Graphics;
import org.mangorage.jnwa.api.screen.Screen;
import org.mangorage.jnwa.api.window.Window;
import org.mangorage.jnwa.api.event.Event;
import org.mangorage.jnwa.api.event.WindowKeyEvent;
import org.mangorage.gamehub.MainMenuScreen;

import java.awt.*;
import java.util.LinkedList;
import java.util.Random;

public final class SnakeGameScreen implements Screen {

    private static final int CELL = 30;


    private final Random random = new Random();
    private final LinkedList<Point> snake = new LinkedList<>();
    private Point food;
    private boolean superFood = false;

    private int dx = 1, dy = 0;
    private long lastMove = 0;
    private long moveDelay = 50;

    private boolean dead = false;

    private Window window;

    @Override
    public void init(Window window) {
        this.window = window;
        window.setTitle("Snake Game -> Score: 0");
        window.setSize(1200, 800);
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
        spawnFood(false);
    }

    // growth counter (add to your class fields)
    private int growAmount = 0;

    @Override
    public void update() {
        if (dead || System.currentTimeMillis() - lastMove < moveDelay)
            return;

        lastMove = System.currentTimeMillis();

        Point h = snake.getFirst();
        Point n = new Point(h.x + dx, h.y + dy);

        int mx = window.width() / CELL, my = window.height() / CELL;

        if (n.x < 0 || n.y < 0 || n.x >= mx || n.y >= my) {
            if (snake.size() <= 8)
                dead = true;
            else
                snake.removeFirst();
            return;
        }

        for (Point p : snake)
            if (p.equals(n)) {
                dead = true; return;
            }

        snake.addFirst(n);

        if (n.equals(food)) {
            spawnFood(true);
            growAmount += superFood ? 10 : 1;
        }

        if (growAmount > 0)
            growAmount--;
        else
            snake.removeLast();

        window.setTitle("Snake Game -> Score: " + snake.size());
    }

    @Override
    public void render(Graphics g, Window window) {
        g.clear(dead ? 0xFF330000 : 0xFF1A1A1A);
        g.setColor(dead ? 0xFF330000 : 0xFF1A1A1A);
        g.fillRect(0, 0, g.width(), g.height());

        g.setColor(Color.BLUE.getRGB());
        boolean head = true;
        for (Point p : snake) {
            g.fillRect(p.x * CELL, p.y * CELL, CELL, CELL);
            if (head) {
                head = false;
                g.setColor(Color.GREEN.getRGB());
            }
        }

        if (food != null) {
            g.setColor(superFood ? Color.CYAN.getRGB() : Color.RED.getRGB());
            g.fillRect(food.x * CELL, food.y * CELL, CELL, CELL);
        }

        int cx = window.width() / 2, cy = window.height() / 2;

        if (dead) {
            drawCentered(g, "Game Over!", 60, Color.RED.getRGB(), cx, cy - 80);
            drawCentered(g, "Press R to restart", 25, 0xFFFFFFFF, cx, cy);
            drawCentered(g, "Press M to go back to Menu", 25, Color.YELLOW.getRGB(), cx, cy + 40);
            drawCentered(g, "Final Score: " + snake.size(), 20, Color.LIGHT_GRAY.getRGB(), cx, cy + 100);
        } else {
            g.setColor(Color.WHITE.getRGB());
            g.renderText("Score: " + snake.size(), 20, 20, 20, false);
            g.setColor(0x88FFFFFF);
            g.renderText("[M] Main Menu", 20, 50, 18, false);
            g.renderText("[R] Reset Game", 20, 75, 18, false);
            g.renderText("[F2] Kill Snake", 20, 100, 18, false);
        }
    }

    private void drawCentered(Graphics g, String text, int size, int color, int cx, int cy) {
        int w = g.getTextWidth(text, size, size > 30);
        g.setColor(color);
        g.renderText(text, cx - (w / 2), cy, size, size > 30);
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

    private void spawnFood(boolean procSuperfood) {
        int maxX = window.width() / CELL;
        int maxY = window.height() / CELL;
        if (maxX <= 0 || maxY <= 0) return;
        food = new Point(random.nextInt(maxX), random.nextInt(maxY));

        if (snake.contains(food))
            spawnFood(false);

        if (procSuperfood)
            superFood = random.nextInt(10) == 2;
    }
}