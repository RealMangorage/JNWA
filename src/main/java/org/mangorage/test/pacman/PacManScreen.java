package org.mangorage.test.pacman;

import org.mangorage.nsapi.api.Graphics;
import org.mangorage.nsapi.api.Screen;
import org.mangorage.nsapi.api.Window;
import org.mangorage.nsapi.api.Event;
import org.mangorage.nsapi.api.event.KeyEvent;
import org.mangorage.test.MainMenuScreen;
import org.mangorage.test.misc.SpriteManager;

import java.awt.*;
import java.util.Random;

public final class PacManScreen implements Screen {

    private static final int CELL = 40;

    private static final int TILE = 38;
    private static final int PADDING = 12;

    // map: 0 empty, 1 wall, 2 pellet
    private final int[][] map = {
            {1,1,1,1,1,1,1,1,1,1,1,1},
            {1,2,2,2,2,2,2,2,2,2,2,1},
            {1,2,1,1,2,1,1,1,2,1,2,1},
            {1,2,1,2,2,2,2,1,2,1,2,1},
            {1,2,1,2,1,1,2,1,2,2,2,1},
            {1,2,2,2,2,1,2,2,2,1,2,1},
            {1,1,1,1,1,1,1,1,1,1,1,1}
    };

    private int px = 1, py = 1;
    private int dx = 1, dy = 0;

    private int gx = 10, gy = 5;

    private int score = 0;
    private int level = 1;
    private boolean dead = false;

    private long lastMove = 0;
    private long delay = 140;

    private final Random random = new Random();
    private Window window;

    // ===== SPRITES =====
    private SpriteManager sprites;

    private Image pacmanTex;
    private Image ghostTex;
    private Image wallTex;
    private Image pelletTex;

    @Override
    public void init(Window window) {
        this.window = window;
        window.setTitle("Pac-Man");

        sprites = new SpriteManager("assets/pacman.png");

        loadSprites();
        reset();
    }

    private void loadSprites() {
        pacmanTex = sprites.getSpriteViaPadding(17, 0, TILE, PADDING);
        ghostTex  = sprites.getSpriteViaPadding(1, 0, TILE, PADDING);

        wallTex   = sprites.getSpriteViaPadding(0, 1, TILE, PADDING);
        pelletTex = sprites.getSprite(506, 611, 18, 18);
    }

    // ===== RESET =====
    private void reset() {
        px = 1; py = 1;
        dx = 1; dy = 0;

        gx = 10; gy = 5;

        score = 0;
        level = 1;
        delay = 140;
        dead = false;

        refill();
    }

    private void refill() {
        for (int y = 0; y < map.length; y++) {
            for (int x = 0; x < map[y].length; x++) {
                if (map[y][x] == 0) map[y][x] = 2;
            }
        }
    }

    // ===== UPDATE =====
    @Override
    public void update() {
        if (dead) return;

        long now = System.currentTimeMillis();
        if (now - lastMove < delay) return;
        lastMove = now;

        movePacman();
        moveGhost();

        if (px == gx && py == gy) {
            dead = true;
            return;
        }

        if (!hasPellets()) {
            nextLevel();
        }
    }

    private void movePacman() {
        int nx = px + dx;
        int ny = py + dy;

        if (map[ny][nx] != 1) {
            px = nx;
            py = ny;

            if (map[ny][nx] == 2) {
                map[ny][nx] = 0;
                score += 10;
            }
        }
    }

    private void moveGhost() {
        int[] d = {-1,0,1,0,-1};

        for (int i = 0; i < 4; i++) {
            int r = random.nextInt(4);

            int nx = gx + d[r];
            int ny = gy + d[r + 1];

            if (ny < 0 || ny >= map.length || nx < 0 || nx >= map[0].length)
                continue;

            if (map[ny][nx] != 1) {
                gx = nx;
                gy = ny;
                return;
            }
        }
    }

    private boolean hasPellets() {
        for (int[] row : map)
            for (int c : row)
                if (c == 2) return true;
        return false;
    }

    private void nextLevel() {
        level++;
        delay = Math.max(60, delay - 10);

        px = 1; py = 1;
        gx = 10; gy = 5;

        refill();
        window.setTitle("Pac-Man - Level " + level);
    }

    // ===== RENDER =====
    @Override
    public void render(Graphics g, Window window) {
        g.clear(0xFF000000);

        for (int y = 0; y < map.length; y++) {
            for (int x = 0; x < map[y].length; x++) {

                if (map[y][x] == 1 && wallTex != null) {
                    g.drawImage(wallTex, x * CELL, y * CELL, CELL, CELL);
                }

                if (map[y][x] == 2 && pelletTex != null) {
                    int size = CELL / 4;
                    int offset = (CELL - size) / 2;

                    g.drawImage(
                            pelletTex,
                            x * CELL + offset,
                            y * CELL + offset,
                            size,
                            size
                    );
                }
            }
        }

        if (pacmanTex != null)
            g.drawImage(pacmanTex, px * CELL, py * CELL, CELL, CELL);

        if (ghostTex != null)
            g.drawImage(ghostTex, gx * CELL, gy * CELL, CELL, CELL);

        g.setColor(Color.WHITE.getRGB());
        g.renderText("Score: " + score, 20, 20, 20, false);
        g.renderText("Level: " + level, 20, 45, 20, false);

        if (dead) {
            int cx = window.width()/2;
            int cy = window.height()/2;

            g.setColor(Color.RED.getRGB());
            g.renderText("GAME OVER", cx - 120, cy, 50, true);
        }
    }

    // ===== INPUT =====
    @Override
    public void onEvent(Event event) {
        if (!(event instanceof KeyEvent ke)) return;
        if (!ke.pressed()) return;

        switch (ke.keyCode()) {
            case java.awt.event.KeyEvent.VK_W -> { dx = 0; dy = -1; }
            case java.awt.event.KeyEvent.VK_S -> { dx = 0; dy = 1; }
            case java.awt.event.KeyEvent.VK_A -> { dx = -1; dy = 0; }
            case java.awt.event.KeyEvent.VK_D -> { dx = 1; dy = 0; }

            case java.awt.event.KeyEvent.VK_R -> reset();

            case java.awt.event.KeyEvent.VK_M ->
                    window.setScreen(new MainMenuScreen());
        }
    }

    @Override
    public void dispose() {}
}