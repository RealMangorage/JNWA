package org.mangorage.test;

import org.mangorage.nsapi.api.Event;
import org.mangorage.nsapi.api.Graphics;
import org.mangorage.nsapi.api.Screen;
import org.mangorage.nsapi.api.Window;
import org.mangorage.nsapi.api.event.WindowKeyEvent;
import org.mangorage.nsapi.api.event.MouseButtonEvent;
import org.mangorage.nsapi.api.event.MouseMoveEvent;
import org.mangorage.nsapi.api.event.MouseScrollEvent;
import org.mangorage.test.pacman.PacManScreen;
import org.mangorage.test.snake.SnakeMainMenuScreen;
import org.mangorage.test.tetris.TetrisScreen;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class MainMenuScreen implements Screen {

    public record Game(
            String name,
            Supplier<Screen> screenSupplier
    ) {}

    private Window window;
    private final List<Game> games = new ArrayList<>();

    private int mouseX;
    private int mouseY;
    private int hoveredIndex = -1;
    private float scrollY = 0;

    @Override
    public void init(Window window) {
        this.window = window;
        window.setTitle("Game Hub");

        games.add(new Game("Snake", SnakeMainMenuScreen::new));
        games.add(new Game("Tetris", TetrisScreen::new));
        games.add(new Game("PacMan", PacManScreen::new));
    }

    @Override
    public void update() {
        hoveredIndex = -1;
        int centerX = window.width() / 2;
        int viewTop = 150;
        int viewBottom = window.height() - 120;

        int itemW = 260;
        int itemH = 35;
        int startY = 180 - (int) scrollY;

        for (int i = 0; i < games.size(); i++) {
            int y = startY + i * 50;
            int x1 = centerX - itemW / 2;
            int x2 = centerX + itemW / 2;
            int y2 = y + itemH;

            // Interaction boundary check
            boolean insideView = y < viewBottom && y2 > viewTop;
            if (!insideView) continue;

            if (mouseX >= x1 && mouseX <= x2 && mouseY >= y && mouseY <= y2) {
                hoveredIndex = i;
                break;
            }
        }

        // Clamp scroll so you can't scroll into the void
        int contentHeight = games.size() * 50;
        int maxScroll = Math.max(0, contentHeight - (viewBottom - viewTop) + 50);
        scrollY = Math.max(0, Math.min(scrollY, maxScroll));
    }

    @Override
    public void render(Graphics g, Window window) {
        g.clear(0xFF000000);

        int centerX = window.width() / 2;
        int viewTop = 150;
        int viewBottom = window.height() - 120;

        // 1. RENDER TITLE
        String title = "GAME HUB";
        int titleW = g.getTextWidth(title, 70, true);
        g.setColor(Color.WHITE.getRGB());
        g.renderText(title, centerX - titleW / 2, 60, 70, true);

        // 2. BACKDROP
        g.setColor(0xFF101010);
        g.fillRect(centerX - 160, viewTop, 320, viewBottom - viewTop);

        // 3. RENDER SCROLLABLE LIST (Manual Clipping Math)
        int startY = 180 - (int) scrollY;

        for (int i = 0; i < games.size(); i++) {
            Game game = games.get(i);
            int y = startY + i * 50;
            int h = 35;
            int w = 260;

            // Skip if completely outside
            if (y + h <= viewTop || y >= viewBottom) continue;

            // Calculate "Visible" Rectangle
            // If the top is above viewTop, bring y down and shrink h
            int drawY = Math.max(y, viewTop);
            int drawBottom = Math.min(y + h, viewBottom);
            int drawH = drawBottom - drawY;

            // Only render if we have a positive height to draw
            if (drawH > 0) {
                boolean hovered = (i == hoveredIndex);

                // Draw Hover Highlight (only if full button or part of it is visible)
                if (hovered) {
                    g.setColor(0x44FFFF00);
                    // Simplified hover rect to match button clipping
                    g.fillRect(centerX - w / 2 - 5, drawY, w + 10, drawH);
                }

                // Draw Button Body
                g.setColor(hovered ? 0xFF2A2A2A : 0xFF1A1A1A);
                g.fillRect(centerX - w / 2, drawY, w, drawH);

                // TEXT CLIPPING:
                // Many simple APIs can't "half-draw" text.
                // We cull text if the button's center isn't fully visible to keep it clean.
                if (y + 10 > viewTop && y + 25 < viewBottom) {
                    g.setColor(hovered ? Color.YELLOW.getRGB() : Color.WHITE.getRGB());
                    int textW = g.getTextWidth(game.name(), 28, false);
                    g.renderText(game.name(), centerX - textW / 2, y + 5, 28, false);
                }
            }
        }

        // 4. RENDER EXIT BUTTON (Always on top)
        int exitY = window.height() - 80;
        boolean exitHover = mouseX >= centerX - 120 && mouseX <= centerX + 120 &&
                mouseY >= exitY && mouseY <= exitY + 40;

        g.setColor(exitHover ? 0xFFAA0000 : 0xFF550000);
        g.fillRect(centerX - 120, exitY, 240, 40);

        g.setColor(Color.WHITE.getRGB());
        int exitW = g.getTextWidth("EXIT", 28, false);
        g.renderText("EXIT", centerX - exitW / 2, exitY + 5, 28, false);
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof MouseMoveEvent me) {
            mouseX = me.x();
            mouseY = me.y();
        }

        if (event instanceof MouseButtonEvent me && me.pressed()) {
            int centerX = window.width() / 2;
            int exitY = window.height() - 80;

            if (mouseX >= centerX - 120 && mouseX <= centerX + 120 &&
                    mouseY >= exitY && mouseY <= exitY + 40) {
                System.exit(0);
                return;
            }

            if (hoveredIndex >= 0 && hoveredIndex < games.size()) {
                window.setScreen(games.get(hoveredIndex).screenSupplier().get());
            }
        }

        if (event instanceof WindowKeyEvent ke && ke.pressed()) {
            // Note: Adjust .keyCode() or .key() based on your specific EventSystem API
            int key = ke.keyCode();
            if (key == java.awt.event.KeyEvent.VK_UP) scrollY -= 50;
            if (key == java.awt.event.KeyEvent.VK_DOWN) scrollY += 50;
        }

        if (event instanceof MouseScrollEvent(double delta)) {
            scrollY -= ((float) delta * 3.5);
        }
    }

    @Override
    public void dispose() {}
}