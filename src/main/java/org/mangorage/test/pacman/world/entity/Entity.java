package org.mangorage.test.pacman.world.entity;

import org.mangorage.jnwapi.api.Graphics;

public abstract class Entity {

    public abstract void update(float delta);
    public abstract void render(Graphics g);
}