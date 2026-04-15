package org.mangorage.jnwapi.api;

public interface Window {

    String id();
    long handle();

    // lifecycle
    void start();
    void close();
    boolean isRunning();

    // visibility
    void setVisible(boolean visible);
    boolean isVisible();

    default void show() {
        setVisible(true);
    }

    default void hide() {
        setVisible(false);
    }

    // screen
    void setScreen(Screen screen);
    Screen getScreen();

    void setPosition(int x, int y);
    void setSize(int x, int y);
    void setSizeLock(boolean lock);

    // ui
    void setTitle(String title);
    void setIcon(byte[] data);

    int width();
    int height();
}