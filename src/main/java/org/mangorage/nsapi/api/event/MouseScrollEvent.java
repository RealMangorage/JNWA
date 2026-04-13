package org.mangorage.nsapi.api.event;

import org.mangorage.nsapi.api.Event;

public record MouseScrollEvent(double delta) implements Event {}
