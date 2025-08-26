package com.mrngwozdz.api.model.response;

import com.mrngwozdz.api.model.EventGroupDTO;
import com.mrngwozdz.database.AppEvent;
import java.util.List;

public record GetEventsResponse(
        List<EventGroupDTO> events
) {
}