package com.capstone.toolScheduler.dto.event;


import java.util.UUID;

import com.capstone.toolScheduler.enums.EventTypes;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.capstone.toolScheduler.dto.event.payload.ParseRequestEventPayload;

public class ParseRequestEvent implements Event<ParseRequestEventPayload> {

    public static EventTypes TYPE = EventTypes.PARSE_REQUEST;

    private String eventId;
    private ParseRequestEventPayload payload;

    public ParseRequestEvent() {
    }

    public ParseRequestEvent(ParseRequestEventPayload payload) {
        this.payload = payload;
        this.eventId = UUID.randomUUID().toString();
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
    public ParseRequestEventPayload getPayload() {
        return payload;
    }
}
