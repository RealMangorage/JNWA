package org.mangorage.jnwapi.api.event;

import org.mangorage.jnwapi.api.Event;

public record MouseScrollEvent(double delta) implements Event {}
