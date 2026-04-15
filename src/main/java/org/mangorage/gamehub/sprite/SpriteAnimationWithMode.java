package org.mangorage.gamehub.sprite;

import java.awt.*;
import java.util.Map;

public final class SpriteAnimationWithMode<T> {

    private final Map<T, SpriteList> sprites;
    private final int frames;
    private final long updateFreq;
    private long lastUpdate = System.currentTimeMillis();

    private int frame = 0;


    public SpriteAnimationWithMode(Map<T, SpriteList> sprites, int frames, long updateFreq) {
        this.sprites = sprites;
        this.frames = frames;
        this.updateFreq = updateFreq;
    }

    public void update() {
        if (System.currentTimeMillis() - lastUpdate >= updateFreq) {
            nextFrame();
            lastUpdate = System.currentTimeMillis();
        }
    }

    public void nextFrame() {
        frame++;
        if (frame >= frames)
            frame = 0;
    }

    public Image get(T mode) {
        return sprites.get(mode).get(frame);
    }
}
