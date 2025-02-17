package com.capstone.toolScheduler.dto.event;

import java.util.UUID;

import com.capstone.toolScheduler.dto.event.payload.ScanRequestEventPayload;
import com.capstone.toolScheduler.enums.EventTypes;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class ScanRequestEvent implements Event<ScanRequestEventPayload> {

    public static EventTypes TYPE = EventTypes.SCAN_REQUEST;

    private String eventId;
    private ScanRequestEventPayload payload;

    public ScanRequestEvent() {
    }

    public ScanRequestEvent(ScanRequestEventPayload payload) {
        this.payload = payload;
        this.eventId = UUID.randomUUID().toString(); // generate unique ID
    }

    

    @Override
    public String getEventId() {
        return eventId;
    }

    @Override
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public EventTypes getType() {
        return TYPE;
    }

    @Override
    public ScanRequestEventPayload getPayload() {
        return payload;
    }
}
