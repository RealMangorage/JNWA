package org.mangorage.test.pacman.entity;

import org.mangorage.nsapi.api.Graphics;

public abstract class Entity {

    public abstract void update(float delta);
    public abstract void render(Graphics g);
}