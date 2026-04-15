package org.mangorage.gamehub.pacman;

import org.mangorage.jnwa.api.event.Event;
import org.mangorage.jnwa.api.event.WindowKeyEvent;
import org.mangorage.gamehub.MainMenuScreen;
import org.mangorage.gamehub.misc.Direction;
import org.mangorage.gamehub.pacman.world.entity.PacManEntity;
import org.mangorage.gamehub.pacman.world.WorldContext;
import org.mangorage.jnwa.api.window.Graphics;
import org.mangorage.jnwa.api.screen.Screen;
import org.mangorage.jnwa.api.window.Window;

import java.awt.event.KeyEvent;

public final class PacManScreen implements Screen {

    private static final int TILE = 32;

    private static final int[][] MAP = {
            {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
            {1,3,2,2,2,2,2,2,2,1,2,2,2,2,2,2,2,3,1},
            {1,2,1,1,2,1,1,1,2,1,2,1,1,1,2,1,1,2,1},
            {1,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,1},
            {1,2,1,1,2,1,2,1,1,1,1,1,2,1,2,1,1,2,1},
            {1,2,2,2,2,1,2,2,2,1,2,2,2,1,2,2,2,2,1},
            {1,1,1,1,2,1,1,1,0,1,0,1,1,1,2,1,1,1,1},
            {0,0,0,1,2,1,0,0,0,0,0,0,0,1,2,1,0,0,0},
            {1,1,1,1,2,1,0,1,1,4,1,1,0,1,2,1,1,1,1},
            {0,0,0,0,2,0,0,1,0,0,0,1,0,0,2,0,0,0,0}, // Side Tunnel Row
            {1,1,1,1,2,1,0,1,1,1,1,1,0,1,2,1,1,1,1},
            {0,0,0,1,2,1,0,0,0,0,0,0,0,1,2,1,0,0,0},
            {1,1,1,1,2,1,0,1,1,1,1,1,0,1,2,1,1,1,1},
            {1,2,2,2,2,2,2,2,2,1,2,2,2,2,2,2,2,2,1},
            {1,2,1,1,2,1,1,1,2,1,2,1,1,1,2,1,1,2,1},
            {1,3,2,1,2,2,2,2,2,0,2,2,2,2,2,1,2,3,1},
            {1,1,2,1,2,1,2,1,1,1,1,1,2,1,2,1,2,1,1},
            {1,2,2,2,2,1,2,2,2,1,2,2,2,1,2,2,2,2,1},
            {1,2,1,1,1,1,1,1,2,1,2,1,1,1,1,1,1,2,1},
            {1,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,1},
            {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1}
    };

    private final WorldContext world = new WorldContext(MAP, TILE);
    private final PacManEntity pacman;

    private Window window;
    private long lastTime = System.nanoTime();
    private int tickCount = 0;

    public PacManScreen() {
        pacman = new PacManEntity(world, 1, 1, true);
    }

    @Override
    public void init(Window window) {
        this.window = window;

        window.setTitle("Java Pac-Man Arcade");

        // Calculate dimensions based on the MAP array
        int width = world.getWidth() * TILE;
        int height = world.getHeight() * TILE;

        // Set the window size
        window.setSize(width + TILE / 2, height + TILE * 4);
        window.setSizeLock(true);
    }

    @Override
    public void update() {
        tickCount++;
        long now = System.nanoTime();
        float delta = (now - lastTime) / 1_000_000_000f;
        lastTime = now;

        pacman.update(delta);
    }

    @Override
    public void render(Graphics g, Window window) {
        g.clear(0xFF000000); // Deep arcade black

// =========================
// 1. CONNECTED NEON WALLS (The Final "No-Nonsense" Version)
// =========================
        g.setColor(0xFF2121DE);
        int pad = 8;
        int thick = 2;

        for (int y = 0; y < world.getHeight(); y++) {
            for (int x = 0; x < world.getWidth(); x++) {
                if (world.getTile(x, y) != 1) continue;

                int px = x * TILE;
                int py = y * TILE;

                boolean u = isWall(x, y - 1);
                boolean d = isWall(x, y + 1);
                boolean l = isWall(x - 1, y);
                boolean r = isWall(x + 1, y);

                // TOP LINE
                if (!u) {
                    int x1 = l ? px : px + pad;
                    int x2 = r ? px + TILE : px + TILE - pad;
                    g.fillRect(x1, py + pad, x2 - x1, thick);
                }

                // BOTTOM LINE
                if (!d) {
                    int x1 = l ? px : px + pad;
                    int x2 = r ? px + TILE : px + TILE - pad;
                    g.fillRect(x1, py + TILE - pad - thick, x2 - x1, thick);
                }

                // LEFT LINE
                if (!l) {
                    // Extension fix: even if there's no wall up/down,
                    // we extend by 'thick' to cap the horizontal lines
                    int y1 = u ? py : py + pad;
                    int y2 = d ? py + TILE : py + TILE - pad;
                    g.fillRect(px + pad, y1, thick, y2 - y1);
                }

                // RIGHT LINE
                if (!r) {
                    int y1 = u ? py : py + pad;
                    int y2 = d ? py + TILE : py + TILE - pad;
                    g.fillRect(px + TILE - pad - thick, y1, thick, y2 - y1);
                }

                // INNER CORNER CAPS (This is the missing link!)
                // This fills the tiny gap where two inner walls meet
                if (u && l && !isWall(x - 1, y - 1)) {
                    g.fillRect(px, py + pad, pad, thick); // Top-left inner
                    g.fillRect(px + pad, py, thick, pad);
                }
                if (u && r && !isWall(x + 1, y - 1)) {
                    g.fillRect(px + TILE - pad, py + pad, pad, thick); // Top-right inner
                    g.fillRect(px + TILE - pad - thick, py, thick, pad);
                }
                if (d && l && !isWall(x - 1, y + 1)) {
                    g.fillRect(px, py + TILE - pad - thick, pad, thick); // Bottom-left inner
                    g.fillRect(px + pad, py + TILE - pad, thick, pad);
                }
                if (d && r && !isWall(x + 1, y + 1)) {
                    g.fillRect(px + TILE - pad, py + TILE - pad - thick, pad, thick); // Bottom-right inner
                    g.fillRect(px + TILE - pad - thick, py + TILE - pad, thick, pad);
                }
            }
        }

        // =========================
        // 2. ROUND PELLETS
        // =========================
        for (int y = 0; y < world.getHeight(); y++) {
            for (int x = 0; x < world.getWidth(); x++) {
                int tile = world.getTile(x, y);
                int px = x * TILE;
                int py = y * TILE;

                if (tile == 2) { // Small Pellet
                    g.setColor(0xFFFFB8AE); // Classic peach/pinkish
                    g.fillOval(px + 14, py + 14, 4, 4);
                } else if (tile == 3) { // Power Pellet
                    // Blink effect: only render if tickCount is even
                    if ((tickCount / 15) % 2 == 0) {
                        g.setColor(0xFFFFB8AE);
                        g.fillOval(px + 10, py + 10, 12, 12);
                    }
                }
            }
        }

        // =========================
        // 3. ICONIC ENTITIES
        // =========================
        pacman.render(g);
    }

    // UPDATE THIS METHOD AT THE BOTTOM OF YOUR CLASS
    private boolean isWall(int x, int y) {
        // For drawing, we want the outside to be "empty"
        // so the border walls actually draw their outer edges.
        if (x < 0 || y < 0 || x >= world.getWidth() || y >= world.getHeight())
            return false;
        return world.getTile(x, y) == 1;
    }

    @Override
    public void onEvent(Event event) {
        if (!(event instanceof WindowKeyEvent(int keyCode, boolean pressed))) return;
        if (!pressed) return;

        switch (keyCode) {
            case KeyEvent.VK_UP -> pacman.setBufferedDirection(Direction.NORTH);
            case KeyEvent.VK_DOWN -> pacman.setBufferedDirection(Direction.SOUTH);
            case KeyEvent.VK_LEFT -> pacman.setBufferedDirection(Direction.WEST);
            case KeyEvent.VK_RIGHT -> pacman.setBufferedDirection(Direction.EAST);
            case KeyEvent.VK_M -> window.setScreen(new MainMenuScreen());
        }
    }

    @Override
    public void dispose() {}
}