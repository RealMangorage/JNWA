package org.mangorage.jnwa.api.event;

import org.mangorage.jnwa.api.Event;

// Keyboard EventSystem
public record WindowKeyEvent(int keyCode, boolean pressed) implements Event {}
