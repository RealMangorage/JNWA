package org.mangorage.jnwapi.api.event;

import org.mangorage.jnwapi.api.Event;

// Mouse Button EventSystem (button: 0=Left, 1=Right, 2=Middle)
public record MouseButtonEvent(int button, boolean pressed) implements Event {}
