package org.mangorage.jnwa.api;

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