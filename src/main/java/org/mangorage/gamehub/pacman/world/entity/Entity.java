package org.mangorage.gamehub.pacman.world.entity;

import org.mangorage.jnwa.api.window.Graphics;

public abstract class Entity {

    public abstract void update(float delta);
    public abstract void render(Graphics g);
}