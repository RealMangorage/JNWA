package org.mangorage.nsapi.api.event;

import org.mangorage.nsapi.api.Event;

// Keyboard Event
public record KeyEvent(int keyCode, boolean pressed) implements Event {}
