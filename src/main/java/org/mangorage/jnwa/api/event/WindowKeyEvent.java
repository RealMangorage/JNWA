package org.mangorage.jnwa.api.event;

// Keyboard EventSystem
public record WindowKeyEvent(int keyCode, boolean pressed) implements Event {}
