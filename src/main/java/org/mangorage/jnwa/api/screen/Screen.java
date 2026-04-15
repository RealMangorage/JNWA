package org.mangorage.jnwa.api.screen;

import org.mangorage.jnwa.api.event.Event;
import org.mangorage.jnwa.api.window.Graphics;
import org.mangorage.jnwa.api.window.Window;

public interface Screen {

    default void init(Window window) {}

    default void update() {}

    default void render(Graphics g, Window window) {
        render(g);
    }

    default void render(Graphics g) {}

    void onEvent(Event event);

    default void dispose() {}
}