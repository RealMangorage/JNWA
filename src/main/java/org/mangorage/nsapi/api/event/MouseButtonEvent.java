package org.mangorage.nsapi.api.event;

import org.mangorage.nsapi.api.Event;

// Mouse Button Event (button: 0=Left, 1=Right, 2=Middle)
public record MouseButtonEvent(int button, boolean pressed) implements Event {}
