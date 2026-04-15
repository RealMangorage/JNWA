package org.mangorage.nsapi.api.event;

import org.mangorage.nsapi.api.Event;

// Keyboard EventSystem
public record WindowKeyEvent(int keyCode, boolean pressed) implements Event {}
