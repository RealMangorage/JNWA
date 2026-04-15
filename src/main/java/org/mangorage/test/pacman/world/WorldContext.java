package org.mangorage.test.pacman.world;

public class WorldContext {

    private final int[][] map;
    private final int tileSize;

    public WorldContext(int[][] map, int tileSize) {
        this.map = map;
        this.tileSize = tileSize;
    }

    public int getTile(int x, int y) {
        return map[y][x];
    }

    public void setTile(int x, int y, int value) {
        map[y][x] = value;
    }

    public int eat(int x, int y) {
        int v = map[y][x];
        if (v == 2 || v == 3) {
            map[y][x] = 0;
        }
        return v;
    }

    public boolean isWalkable(int x, int y) {
        return map[y][x] != 1 && map[y][x] != 4;
    }

    public int getWidth() {
        return map[0].length;
    }

    public int getHeight() {
        return map.length;
    }

    public int getTileSize() {
        return tileSize;
    }
}