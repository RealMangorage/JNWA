package org.mangorage.gamehub.sprite;

import java.awt.*;
import java.util.List;

public final class SpriteList {

    private final List<Image> frames;

    public SpriteList(List<Image> frames) {
        this.frames = frames;
    }

    public Image get(int index) {
        return frames.get(index);
    }

    public int getSize() {
        return frames.size();
    }
}