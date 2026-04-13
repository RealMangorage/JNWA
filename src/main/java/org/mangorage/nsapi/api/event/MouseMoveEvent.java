package org.mangorage.nsapi.api.event;

import org.mangorage.nsapi.api.Event;

// Mouse Movement Event
public record MouseMoveEvent(int x, int y) implements Event {}
