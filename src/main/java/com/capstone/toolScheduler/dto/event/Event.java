package com.capstone.toolScheduler.dto.event;

import com.capstone.toolScheduler.enums.EventTypes;

public interface Event<T> {
    String getEventId();

    EventTypes getType();

    T getPayload();
}
