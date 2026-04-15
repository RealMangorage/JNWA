package org.mangorage.jnwa.api.event;

import org.mangorage.jnwa.api.Event;

// Mouse Movement EventSystem
public record MouseMoveEvent(int x, int y) implements Event {}
