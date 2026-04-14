package org.mangorage.test.sprite;

import java.awt.*;

public final class SpriteAnimation {

    private final SpriteList spriteList;

    private final long updateFreq;
    private long lastUpdate = System.currentTimeMillis();
    private int frame = 0;


    public SpriteAnimation(SpriteList spriteList, long updateFreq) {
        this.spriteList = spriteList;
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
        if (frame >= spriteList.getSize())
            frame = 0;
    }

    public Image get() {
        return spriteList.get(frame);
    }
}
