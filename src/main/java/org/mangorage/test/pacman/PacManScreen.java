package org.mangorage.test.pacman;

import org.mangorage.nsapi.api.*;
import org.mangorage.nsapi.api.event.WindowKeyEvent;
import org.mangorage.test.misc.Direction;
import org.mangorage.test.pacman.entity.Entity;
import org.mangorage.test.pacman.entity.PacManEntity;
import org.mangorage.test.pacman.world.WorldContext;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

public final class PacManScreen implements Screen {

    private static final int TILE = 32;

    private static final int[][] MAP = {
            {1,1,1,1,1,1,1,1,1,1},
            {1,0,0,0,0,0,0,0,0,1},
            {1,0,1,1,0,1,1,0,0,1},
            {1,0,0,1,0,0,1,0,0,1},
            {1,0,0,0,0,0,0,0,0,1},
            {1,1,1,1,1,1,1,1,1,1}
    };

    private final WorldContext world = new WorldContext(MAP, TILE);
    private final List<Entity> entities = new ArrayList<>();

    private Window window;

    private long lastTime = System.nanoTime();

    public PacManScreen() {
        entities.add(new PacManEntity(world, 1, 1));
    }

    @Override
    public void init(Window window) {
        this.window = window;
        window.setTitle("Pac-Man Entity System");
    }

    @Override
    public void update() {
        long now = System.nanoTime();
        float delta = (now - lastTime) / 1_000_000_000f;
        lastTime = now;

        for (var e : entities) {
            e.update(delta);
        }
    }

    @Override
    public void render(Graphics g, Window window) {
        g.clear(0xFF000000);

        // draw walls
        for (int y = 0; y < MAP.length; y++) {
            for (int x = 0; x < MAP[y].length; x++) {
                if (MAP[y][x] == 1) {
                    g.drawRect(x * TILE, y * TILE, TILE, TILE);
                }
            }
        }

        // draw entities
        for (var e : entities) {
            e.render(g);
        }
    }

    @Override
    public void onEvent(Event event) {
        if (!(event instanceof WindowKeyEvent(int keyCode, boolean pressed))) return;
        if (!pressed) return;

        for (var e : entities) {
            if (e instanceof PacManEntity p) {
                switch (keyCode) {
                    case KeyEvent.VK_UP -> p.setBufferedDirection(Direction.NORTH);
                    case KeyEvent.VK_DOWN -> p.setBufferedDirection(Direction.SOUTH);
                    case KeyEvent.VK_LEFT -> p.setBufferedDirection(Direction.WEST);
                    case KeyEvent.VK_RIGHT -> p.setBufferedDirection(Direction.EAST);
                }
            }
        }
    }

    @Override
    public void dispose() {}
}