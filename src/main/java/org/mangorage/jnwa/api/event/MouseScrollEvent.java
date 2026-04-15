package org.mangorage.jnwa.api.event;

import org.mangorage.jnwa.api.Event;

public record MouseScrollEvent(double delta) implements Event {}
