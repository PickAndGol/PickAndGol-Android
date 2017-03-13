package io.keepcoding.pickandgol.model.mapper;

import io.keepcoding.pickandgol.manager.net.response.EventsResponse;
import io.keepcoding.pickandgol.model.Event;

public class EventsDataToEventMapper {
    public Event map(EventsResponse.EventsData data) {
        Event event = new Event(data.getId(),
                data.getName(),
                data.getDate(),
                data.getDate(),
                data.getPhotoUrl(),
                data.getCategory(),
                data.getPubs());

        return event;
    }
}
