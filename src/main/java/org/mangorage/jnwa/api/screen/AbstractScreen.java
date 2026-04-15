package org.mangorage.jnwa.api.screen;

import org.mangorage.jnwa.api.event.Event;
import org.mangorage.jnwa.api.event.MouseButtonEvent;
import org.mangorage.jnwa.api.event.MouseMoveEvent;
import org.mangorage.jnwa.api.event.MouseScrollEvent;
import org.mangorage.jnwa.api.event.WindowKeyEvent;
import org.mangorage.jnwa.api.window.Window;

public abstract class AbstractScreen implements Screen {

    private Window window;

    @Override
    public void init(Window window) {
        this.window = window;
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof WindowKeyEvent(int keyCode, boolean pressed))
            onKeyEvent(keyCode, pressed);
        if (event instanceof MouseScrollEvent(int delta))
            onMouseScrollEvent(delta);
        if (event instanceof MouseMoveEvent(int x, int y))
            onMouseMoveEvent(x, y);
        if (event instanceof MouseButtonEvent(int button, boolean pressed))
            onMouseButtonEvent(button, pressed);
    }

    public Window getWindow() {
        return window;
    }

    public abstract void onKeyEvent(int keyCode, boolean pressed);
    public abstract void onMouseScrollEvent(double delta);
    public abstract void onMouseMoveEvent(int x, int y);
    public abstract void onMouseButtonEvent(int button, boolean pressed);
}
