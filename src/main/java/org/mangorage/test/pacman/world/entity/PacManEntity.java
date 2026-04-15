package org.mangorage.test.pacman.world.entity;

import org.mangorage.jnwa.api.Graphics;
import org.mangorage.test.misc.Direction;
import org.mangorage.test.pacman.world.WorldContext;
import org.mangorage.test.sprite.*;

import java.util.List;
import java.util.Map;

public final class PacManEntity extends Entity {

    record DirectionWithMode(Direction direction, boolean agro) {};

    private static final int TILE = 32;

    // ===== STATE =====
    private int tileX, tileY;

    private float pixelX, pixelY;

    private int targetX, targetY;

    private Direction direction = Direction.EAST;
    private Direction bufferedDirection = Direction.EAST;
    private boolean agro = false;

    private float speed = 225.5f; // tiles per second

    private final WorldContext world;
    private final boolean update;

    // ===== ANIMATION =====
    private final SpriteAnimationWithMode<DirectionWithMode> anim;

    public PacManEntity(WorldContext world, int startX, int startY, boolean update) {
        this.world = world;

        this.tileX = startX;
        this.tileY = startY;

        this.targetX = startX;
        this.targetY = startY;

        this.pixelX = startX * TILE;
        this.pixelY = startY * TILE;

        this.update = update;

        SpriteManager sprites = new SpriteManager("assets/pacman.png");

        SpriteList east_normal = new SpriteList(List.of(
                sprites.getSpriteViaPadding(17, 0, 38, 12),
                sprites.getSpriteViaPadding(17, 1, 38, 12),
                sprites.getSpriteViaPadding(17, 2, 38, 12)
        ));

        SpriteList south_normal = new SpriteList(List.of(
                sprites.getSpriteViaPadding(17, 3, 38, 12),
                sprites.getSpriteViaPadding(17, 4, 38, 12),
                sprites.getSpriteViaPadding(17, 5, 38, 12)
        ));

        SpriteList west_normal = new SpriteList(List.of(
                sprites.getSpriteViaPadding(17, 6, 38, 12),
                sprites.getSpriteViaPadding(17, 7, 38, 12),
                sprites.getSpriteViaPadding(17, 8, 38, 12)
        ));

        SpriteList north_normal = new SpriteList(List.of(
                sprites.getSpriteViaPadding(17, 9, 38, 12),
                sprites.getSpriteViaPadding(17, 10, 38, 12),
                sprites.getSpriteViaPadding(17, 11, 38, 12)
        ));

        SpriteList east_agro = new SpriteList(List.of(
                sprites.getSpriteViaPadding(18, 0, 38, 12),
                sprites.getSpriteViaPadding(18, 1, 38, 12),
                sprites.getSpriteViaPadding(18, 2, 38, 12)
        ));

        SpriteList south_agro = new SpriteList(List.of(
                sprites.getSpriteViaPadding(18, 3, 38, 12),
                sprites.getSpriteViaPadding(18, 4, 38, 12),
                sprites.getSpriteViaPadding(18, 5, 38, 12)
        ));

        SpriteList west_agro = new SpriteList(List.of(
                sprites.getSpriteViaPadding(18, 6, 38, 12),
                sprites.getSpriteViaPadding(18, 7, 38, 12),
                sprites.getSpriteViaPadding(18, 8, 38, 12)
        ));

        SpriteList north_agro = new SpriteList(List.of(
                sprites.getSpriteViaPadding(18, 9, 38, 12),
                sprites.getSpriteViaPadding(18, 10, 38, 12),
                sprites.getSpriteViaPadding(18, 11, 38, 12)
        ));

        anim = new SpriteAnimationWithMode<>(
                Map.of(
                        new DirectionWithMode(Direction.EAST, false), east_normal,
                        new DirectionWithMode(Direction.SOUTH, false), south_normal,
                        new DirectionWithMode(Direction.WEST, false), west_normal,
                        new DirectionWithMode(Direction.NORTH, false), north_normal,

                        new DirectionWithMode(Direction.EAST, true), east_agro,
                        new DirectionWithMode(Direction.SOUTH, true), south_agro,
                        new DirectionWithMode(Direction.WEST, true), west_agro,
                        new DirectionWithMode(Direction.NORTH, true), north_agro
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

        // Ghost Placeholder
        if (!update) return;

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

    public boolean isAgro() {
        return agro;
    }

    public void setAgro(boolean agro) {
        this.agro = agro;
    }

    // ===== RENDER =====
    @Override
    public void render(Graphics g) {
        g.drawImage(
                anim.get(new DirectionWithMode(direction, agro)),
                (int) pixelX,
                (int) pixelY,
                TILE,
                TILE
        );
    }
}