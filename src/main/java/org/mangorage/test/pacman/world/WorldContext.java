package org.mangorage.test.pacman.world;

public final class WorldContext {

    private final int[][] map;
    private final int tileSize;

    public WorldContext(int[][] map, int tileSize) {
        this.map = map;
        this.tileSize = tileSize;
    }

    public int tileSize() {
        return tileSize;
    }

    public boolean isWalkable(int x, int y) {
        if (y < 0 || y >= map.length) return false;
        if (x < 0 || x >= map[0].length) return false;
        return map[y][x] == 0;
    }
}