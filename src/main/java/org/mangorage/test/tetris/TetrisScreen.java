package org.mangorage.test.tetris;

import org.mangorage.nsapi.api.Event;
import org.mangorage.nsapi.api.Graphics;
import org.mangorage.nsapi.api.Screen;
import org.mangorage.nsapi.api.Window;
import org.mangorage.nsapi.api.event.KeyEvent;
import org.mangorage.test.MainMenuScreen;

import java.util.Random;

public final class TetrisScreen implements Screen {
    private static final int COLS = 10;
    private static final int ROWS = 20;
    private static final int CELL = 30;

    // Game State
    private int[][] board = new int[ROWS][COLS]; // 0 = empty, else = color hex
    private Window window;
    private final Random random = new Random();
    private boolean dead = false;
    private int score = 0;

    // Current Piece
    private int[][] currentPiece;
    private int pieceX, pieceY;
    private int pieceColor;

    // Timing
    private long lastFall = 0;
    private long fallDelay = 500; // Gravity speed

    // Tetrominoes (I, J, L, O, S, T, Z)
    private static final int[][][] SHAPES = {
            {{1, 1, 1, 1}}, // I
            {{1, 0, 0}, {1, 1, 1}}, // J
            {{0, 0, 1}, {1, 1, 1}}, // L
            {{1, 1}, {1, 1}}, // O
            {{0, 1, 1}, {1, 1, 0}}, // S
            {{0, 1, 0}, {1, 1, 1}}, // T
            {{1, 1, 0}, {0, 1, 1}}  // Z
    };

    private static final int[] COLORS = {
            0xFF00FFFF, 0xFF0000FF, 0xFFFFA500, 0xFFFFFF00, 0xFF00FF00, 0xFF800080, 0xFFFF0000
    };

    @Override
    public void init(Window window) {
        this.window = window;
        window.setTitle("Tetris");
        spawnPiece();
    }

    private void spawnPiece() {
        int idx = random.nextInt(SHAPES.length);
        currentPiece = SHAPES[idx];
        pieceColor = COLORS[idx];
        pieceX = COLS / 2 - currentPiece[0].length / 2;
        pieceY = 0;

        if (collision(pieceX, pieceY, currentPiece)) {
            dead = true;
        }
    }

    private boolean collision(int nx, int ny, int[][] shape) {
        for (int row = 0; row < shape.length; row++) {
            for (int col = 0; col < shape[row].length; col++) {
                if (shape[row][col] == 0) continue;
                int targetX = nx + col;
                int targetY = ny + row;

                if (targetX < 0 || targetX >= COLS || targetY >= ROWS) return true;
                if (targetY >= 0 && board[targetY][targetX] != 0) return true;
            }
        }
        return false;
    }

    private void lockPiece() {
        for (int row = 0; row < currentPiece.length; row++) {
            for (int col = 0; col < currentPiece[row].length; col++) {
                if (currentPiece[row][col] != 0) {
                    board[pieceY + row][pieceX + col] = pieceColor;
                }
            }
        }
        clearLines();
        spawnPiece();
    }

    private void clearLines() {
        for (int row = ROWS - 1; row >= 0; row--) {
            boolean full = true;
            for (int col = 0; col < COLS; col++) {
                if (board[row][col] == 0) { full = false; break; }
            }
            if (full) {
                for (int r = row; r > 0; r--) board[r] = board[r - 1];
                board[0] = new int[COLS];
                score += 100;
                row++; // Check same row index again
            }
        }
    }

    private void rotate() {
        int[][] rotated = new int[currentPiece[0].length][currentPiece.length];
        for (int r = 0; r < currentPiece.length; r++) {
            for (int c = 0; c < currentPiece[0].length; c++) {
                rotated[c][currentPiece.length - 1 - r] = currentPiece[r][c];
            }
        }
        if (!collision(pieceX, pieceY, rotated)) currentPiece = rotated;
    }

    @Override
    public void update() {
        if (dead) return;
        long now = System.currentTimeMillis();
        if (now - lastFall > fallDelay) {
            if (!collision(pieceX, pieceY + 1, currentPiece)) {
                pieceY++;
            } else {
                lockPiece();
            }
            lastFall = now;
        }
    }

    @Override
    public void render(Graphics g, Window window) {
        g.clear(0xFF000000);
        int offsetX = (window.width() - COLS * CELL) / 2;
        int offsetY = (window.height() - ROWS * CELL) / 2;

        // Background Board
        g.setColor(0xFF111111);
        g.fillRect(offsetX, offsetY, COLS * CELL, ROWS * CELL);

        // Draw Locked Blocks
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (board[r][c] != 0) {
                    g.setColor(board[r][c]);
                    g.fillRect(offsetX + c * CELL + 1, offsetY + r * CELL + 1, CELL - 2, CELL - 2);
                }
            }
        }

        // Draw Current Piece
        if (!dead) {
            g.setColor(pieceColor);
            for (int r = 0; r < currentPiece.length; r++) {
                for (int c = 0; c < currentPiece[r].length; c++) {
                    if (currentPiece[r][c] != 0) {
                        g.fillRect(offsetX + (pieceX + c) * CELL + 1, offsetY + (pieceY + r) * CELL + 1, CELL - 2, CELL - 2);
                    }
                }
            }
        }

        // UI
        g.setColor(0xFFFFFFFF);
        g.renderText("Score: " + score, 20, 20, 20, false);
        if (dead) {
            g.renderText("GAME OVER - Press R", window.width() / 2 - 100, window.height() / 2, 30, true);
        }
    }

    @Override
    public void onEvent(Event event) {
        if (!(event instanceof KeyEvent ke) || !ke.pressed()) return;
        int code = ke.keyCode();

        if (code == java.awt.event.KeyEvent.VK_M) window.setScreen(new MainMenuScreen());
        if (dead) {
            if (code == java.awt.event.KeyEvent.VK_R) { board = new int[ROWS][COLS]; score = 0; dead = false; spawnPiece(); }
            return;
        }

        if (code == java.awt.event.KeyEvent.VK_A && !collision(pieceX - 1, pieceY, currentPiece)) pieceX--;
        if (code == java.awt.event.KeyEvent.VK_D && !collision(pieceX + 1, pieceY, currentPiece)) pieceX++;
        if (code == java.awt.event.KeyEvent.VK_S && !collision(pieceX, pieceY + 1, currentPiece)) pieceY++;
        if (code == java.awt.event.KeyEvent.VK_W) rotate();
        if (code == java.awt.event.KeyEvent.VK_SPACE) {
            while (!collision(pieceX, pieceY + 1, currentPiece)) pieceY++;
            lockPiece();
        }
    }

    @Override public void dispose() {}
}
