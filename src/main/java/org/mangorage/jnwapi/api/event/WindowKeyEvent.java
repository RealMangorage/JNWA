package org.mangorage.jnwapi.api.event;

import org.mangorage.jnwapi.api.Event;

// Keyboard EventSystem
public record WindowKeyEvent(int keyCode, boolean pressed) implements Event {}
