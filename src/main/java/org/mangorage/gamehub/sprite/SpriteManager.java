package org.mangorage.gamehub.sprite;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public final class SpriteManager {

    private final BufferedImage sheet;

    private final Map<String, BufferedImage> cache = new HashMap<>();

    public SpriteManager(String resourcePath) {
        this.sheet = load(resourcePath);
    }

    private BufferedImage load(String path) {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(path)) {
            if (in == null) {
                throw new RuntimeException("Missing sprite sheet: " + path);
            }
            return ImageIO.read(in);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // =========================================================
    // GRID-BASED SPRITE (atlas with padding)
    // =========================================================
    public BufferedImage getSpriteViaPadding(int col, int row, int size, int padding) {
        String key = "G:" + col + "," + row + "," + size + "," + padding;

        return cache.computeIfAbsent(key, k -> {
            int step = size + padding;

            int x = col * step;
            int y = row * step;

            return sheet.getSubimage(x, y, size, size);
        });
    }

    // =========================================================
    // RAW PIXEL SPRITE
    // =========================================================
    public BufferedImage getSprite(int x, int y, int w, int h) {
        String key = "R:" + x + "," + y + "," + w + "," + h;

        return cache.computeIfAbsent(key, k ->
                sheet.getSubimage(x, y, w, h)
        );
    }
}