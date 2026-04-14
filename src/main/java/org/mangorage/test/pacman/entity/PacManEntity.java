package org.mangorage.test.pacman.entity;

import org.mangorage.nsapi.api.Graphics;
import org.mangorage.test.misc.Direction;
import org.mangorage.test.pacman.entity.Entity;
import org.mangorage.test.pacman.world.WorldContext;
import org.mangorage.test.sprite.*;

import java.util.List;
import java.util.Map;

public final class PacManEntity extends Entity {

    private static final int TILE = 32;

    // ===== STATE =====
    private int tileX, tileY;

    private float pixelX, pixelY;

    private int targetX, targetY;

    private Direction direction = Direction.EAST;
    private Direction bufferedDirection = Direction.EAST;

    private float speed = 225.5f; // tiles per second

    private final WorldContext world;

    // ===== ANIMATION =====
    private final SpriteAnimationWithMode<Direction> anim;

    public PacManEntity(WorldContext world, int startX, int startY) {
        this.world = world;

        this.tileX = startX;
        this.tileY = startY;

        this.targetX = startX;
        this.targetY = startY;

        this.pixelX = startX * TILE;
        this.pixelY = startY * TILE;

        SpriteManager sprites = new SpriteManager("assets/pacman.png");

        SpriteList east = new SpriteList(List.of(
                sprites.getSpriteViaPadding(17, 0, 38, 12),
                sprites.getSpriteViaPadding(17, 1, 38, 12),
                sprites.getSpriteViaPadding(17, 2, 38, 12)
        ));

        SpriteList south = new SpriteList(List.of(
                sprites.getSpriteViaPadding(17, 3, 38, 12),
                sprites.getSpriteViaPadding(17, 4, 38, 12),
                sprites.getSpriteViaPadding(17, 5, 38, 12)
        ));

        SpriteList west = new SpriteList(List.of(
                sprites.getSpriteViaPadding(17, 6, 38, 12),
                sprites.getSpriteViaPadding(17, 7, 38, 12),
                sprites.getSpriteViaPadding(17, 8, 38, 12)
        ));

        SpriteList north = new SpriteList(List.of(
                sprites.getSpriteViaPadding(17, 9, 38, 12),
                sprites.getSpriteViaPadding(17, 10, 38, 12),
                sprites.getSpriteViaPadding(17, 11, 38, 12)
        ));

        anim = new SpriteAnimationWithMode<>(
                Map.of(
                        Direction.EAST, east,
                        Direction.SOUTH, south,
                        Direction.WEST, west,
                        Direction.NORTH, north
                ),
                3,
                100
        );
    }

    // ===== INPUT =====
    public void setBufferedDirection(Direction dir) {
        this.bufferedDirection = dir;
    }

    // ===== UPDATE =====
    @Override
    public void update(float delta) {

        anim.update();

        float step = speed * delta;

        // === ARRIVED AT TILE ===
        if (tileX == targetX && tileY == targetY) {

            // try buffered turn first
            int bx = tileX + bufferedDirection.dx();
            int by = tileY + bufferedDirection.dy();

            if (world.isWalkable(bx, by)) {
                direction = bufferedDirection;
            }

            // continue moving forward if possible
            int fx = tileX + direction.dx();
            int fy = tileY + direction.dy();

            if (world.isWalkable(fx, fy)) {
                targetX = fx;
                targetY = fy;
            }
        }

        float targetPixelX = targetX * TILE;
        float targetPixelY = targetY * TILE;

        float dx = targetPixelX - pixelX;
        float dy = targetPixelY - pixelY;

        float dist = (float) Math.sqrt(dx * dx + dy * dy);

        if (dist > 0.001f) {
            pixelX += (dx / dist) * step;
            pixelY += (dy / dist) * step;
        }

        // snap when close enough
        if (dist < 0.5f) {
            pixelX = targetPixelX;
            pixelY = targetPixelY;
            tileX = targetX;
            tileY = targetY;
        }
    }

    // ===== RENDER =====
    @Override
    public void render(Graphics g) {
        g.drawImage(
                anim.get(direction),
                (int) pixelX,
                (int) pixelY,
                TILE,
                TILE
        );
    }
}