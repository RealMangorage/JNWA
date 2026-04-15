package org.mangorage.nsapi.api.event;

import org.mangorage.nsapi.api.Event;

// Mouse Movement EventSystem
public record MouseMoveEvent(int x, int y) implements Event {}
