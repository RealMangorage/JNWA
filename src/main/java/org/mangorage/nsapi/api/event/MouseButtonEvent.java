package org.mangorage.nsapi.api.event;

import org.mangorage.nsapi.api.Event;

// Mouse Button EventSystem (button: 0=Left, 1=Right, 2=Middle)
public record MouseButtonEvent(int button, boolean pressed) implements Event {}
