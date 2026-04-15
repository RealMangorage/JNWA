package org.mangorage.test.pacman.world.entity;

import org.mangorage.jnwa.api.Graphics;

public abstract class Entity {

    public abstract void update(float delta);
    public abstract void render(Graphics g);
}