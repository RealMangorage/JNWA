package org.mangorage.gamehub.tetris;

import org.mangorage.jnwa.api.screen.AbstractScreen;
import org.mangorage.jnwa.api.window.Graphics;
import org.mangorage.jnwa.api.window.Window;
import org.mangorage.gamehub.MainMenuScreen;

import java.awt.event.KeyEvent;
import java.util.Random;

public final class TetrisScreen extends AbstractScreen {
    // --- CHEAT CONFIG ---
    private static final boolean CHEAT_ENABLED = false; // Toggle this to true to win forever
    private static final int[][] CHEAT_SHAPE = shape("##########\n##########\n##########\n##########");
    // --------------------

    private static int[][] shape(String raw) {
        String[] lines = raw.strip().split("\\R");
        int height = lines.length;
        int width = 0;
        for (String line : lines)
            width = Math.max(width, line.length());

        int[][] result = new int[height][width];
        for (int y = 0; y < height; y++) {
            String line = lines[y];
            for (int x = 0; x < width; x++) {
                char c = x < line.length() ? line.charAt(x) : '$';
                result[y][x] = (c == '#') ? 1 : 0;
            }
        }
        return result;
    }

    private static final int COLS = 10;
    private static final int ROWS = 20;
    private static final int CELL = 30;

    private int[][] board = new int[ROWS][COLS];
    private final Random random = new Random();
    private boolean dead = false;
    private int score = 0;
    private int linesCleared = 0;
    private int level = 0;

    private int[][] currentPiece;
    private int pieceX, pieceY, pieceColor;
    private int[][] nextPiece;
    private int nextPieceColor;

    private long lastFall = 0;
    private long fallDelay = 500;

    private static final int[][][] SHAPES = {
            shape("####"), // 0: I
            shape("#$$\n###"), // 1: J
            shape("$$#\n###"), // 2: L
            shape("##\n##"), // 3: O
            shape("$##\n##$"), // 4: S
            shape("$#$\n###"), // 5: T
            shape("##$\n$##"), // 6: Z
    };

    private static final int[][] COLORS = {
            {0xFFFFFFFF, 0xFF888888},
            {0xFF00FFFF, 0xFF22FFFF},
            {0xFF0000FF, 0xFF2222FF},
            {0xFFFFA500, 0xFFFFB522},
            {0xFFFFFF00, 0xFFFFFFAA},
            {0xFF00FF00, 0xFF22FF22},
            {0xFF800080, 0xFFA000A0},
            {0xFFFF0000, 0xFFFF2222}
    };

    @Override
    public void init(Window window) {
        super.init(window);
        window.setTitle("Retro Tetris" + (CHEAT_ENABLED ? " [CHEAT MODE]" : ""));
        window.setSize(800, 800);
        window.setSizeLock(true);
        spawnPiece();
    }

    private void spawnPiece() {
        if (nextPiece == null)
            generateNextPiece();

        currentPiece = nextPiece;
        pieceColor = nextPieceColor;
        pieceX = COLS / 2 - currentPiece[0].length / 2;
        pieceY = 0;

        generateNextPiece();

        if (collision(pieceX, pieceY, currentPiece))
            dead = true;
    }

    private void generateNextPiece() {
        int idx = random.nextInt(SHAPES.length);

        nextPiece = SHAPES[idx];
        nextPieceColor = COLORS[idx + 1][random.nextInt(COLORS[idx + 1].length)];

        if (CHEAT_ENABLED)
            nextPiece = CHEAT_SHAPE;
    }

    private boolean collision(int nx, int ny, int[][] shape) {
        for (int r = 0; r < shape.length; r++) {
            for (int c = 0; c < shape[r].length; c++) {
                if (shape[r][c] == 0)
                    continue;

                int tx = nx + c, ty = ny + r;

                if (tx < 0 || tx >= COLS || ty >= ROWS)
                    return true;
                if (ty >= 0 && board[ty][tx] != 0)
                    return true;
            }
        }
        return false;
    }

    private void lockPiece() {
        for (int r = 0; r < currentPiece.length; r++) {
            for (int c = 0; c < currentPiece[r].length; c++) {
                if (currentPiece[r][c] != 0)
                    board[pieceY + r][pieceX + c] = pieceColor;
            }
        }
        clearLines();
        spawnPiece();
    }

    private void clearLines() {
        int count = 0;
        for (int row = ROWS - 1; row >= 0; row--) {
            boolean full = true;
            for (int col = 0; col < COLS; col++)
                if (board[row][col] == 0) {
                    full = false;
                    break;
                }

            if (full) {
                for (int r = row; r > 0; r--)
                    board[r] = board[r - 1];

                board[0] = new int[COLS];
                count++;
                row++;
            }
        }
        if (count > 0) {
            linesCleared += count;
            score += (count * 100) * (level + 1);
            level = linesCleared / 10;
            fallDelay = Math.max(2, 500 - (level * 40));
        }
    }

    private void rotate() {
        int[][] rot = new int[currentPiece[0].length][currentPiece.length];
        for (int r = 0; r < currentPiece.length; r++)
            for (int c = 0; c < currentPiece[0].length; c++)
                rot[c][currentPiece.length - 1 - r] = currentPiece[r][c];

        if (!collision(pieceX, pieceY, rot))
            currentPiece = rot;
    }

    @Override
    public void update() {
        if (dead) return;
        long now = System.currentTimeMillis();
        if (now - lastFall > fallDelay) {
            if (!collision(pieceX, pieceY + 1, currentPiece))
                pieceY++;
            else
                lockPiece();

            lastFall = now;
        }
    }

    @Override
    public void render(Graphics g, Window window) {
        g.clear(0xFF000000);
        int bx = 50, by = 80;

        g.setColor(0xFFFFFFFF);
        g.drawRect(bx - 2, by - 2, (COLS * CELL) + 4, (ROWS * CELL) + 4);
        g.setColor(0xFF111111);
        g.fillRect(bx, by, COLS * CELL, ROWS * CELL);

        for (int r = 0; r < ROWS; r++)
            for (int c = 0; c < COLS; c++)
                if (board[r][c] != 0)
                    drawBlock(g, bx + c * CELL, by + r * CELL, board[r][c]);

        if (!dead)
            for (int r = 0; r < currentPiece.length; r++)
                for (int c = 0; c < currentPiece[r].length; c++)
                    if (currentPiece[r][c] != 0)
                        drawBlock(g, bx + (pieceX + c) * CELL, by + (pieceY + r) * CELL, pieceColor);

        int sx = bx + (COLS * CELL) + 50;

        drawBox(g, "NEXT", sx, by, 150, 120);
        if (nextPiece != null) {
            int nx = sx + (150 - nextPiece[0].length * CELL) / 2;
            int ny = by + (120 - nextPiece.length * CELL) / 2;
            for (int r = 0; r < nextPiece.length; r++)
                for (int c = 0; c < nextPiece[r].length; c++)
                    if (nextPiece[r][c] != 0)
                        drawBlock(g, nx + c * CELL, ny + r * CELL, nextPieceColor);
        }

        drawStat(g, "SCORE", String.format("%06d", score), sx, by + 160);
        drawStat(g, "LINES", String.format("%03d", linesCleared), sx, by + 260);
        drawStat(g, "LEVEL", String.format("%02d", level), sx, by + 360);

        if (dead) {
            g.setColor(0xFFFF0000);
            g.renderText("GAME OVER - PRESS R", bx + 10, by + 300, 24, false);
        }
    }

    private void drawBlock(Graphics g, int x, int y, int color) {
        g.setColor(color);
        g.fillRect(x + 1, y + 1, CELL - 2, CELL - 2);
    }

    private void drawBox(Graphics g, String label, int x, int y, int w, int h) {
        g.setColor(0xFFFFFFFF);
        g.drawRect(x, y, w, h);
        g.renderText(label, x, y - 20, 18, false);
    }

    private void drawStat(Graphics g, String label, String val, int x, int y) {
        drawBox(g, label, x, y, 180, 60);
        g.renderText(val, x + 15, y + 22, 22, false);
    }

    @Override
    public void onKeyEvent(int keyCode, boolean pressed) {
        if (!pressed) return; // Only trigger on initial press

        if (keyCode == KeyEvent.VK_M) {
            getWindow().setScreen(new MainMenuScreen());
            return;
        }

        if (dead) {
            if (keyCode == KeyEvent.VK_R) {
                board = new int[ROWS][COLS];
                score = 0;
                linesCleared = 0;
                level = 0;
                dead = false;
                spawnPiece();
            }
            return;
        }

        if (keyCode == KeyEvent.VK_A && !collision(pieceX - 1, pieceY, currentPiece))
            pieceX--;

        if (keyCode == KeyEvent.VK_D && !collision(pieceX + 1, pieceY, currentPiece))
            pieceX++;

        if (keyCode == KeyEvent.VK_S && !collision(pieceX, pieceY + 1, currentPiece))
            pieceY++;

        if (keyCode == KeyEvent.VK_W)
            rotate();

        if (keyCode == KeyEvent.VK_SPACE) {
            while (!collision(pieceX, pieceY + 1, currentPiece))
                pieceY++;
            lockPiece();
        }
    }

    @Override
    public void onMouseScrollEvent(double d) {}
    @Override
    public void onMouseMoveEvent(int x, int y) {}
    @Override
    public void onMouseButtonEvent(int b, boolean p) {}
    @Override
    public void dispose() {}
}
