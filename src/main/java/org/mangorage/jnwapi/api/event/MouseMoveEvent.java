package org.mangorage.jnwapi.api.event;

import org.mangorage.jnwapi.api.Event;

// Mouse Movement EventSystem
public record MouseMoveEvent(int x, int y) implements Event {}
