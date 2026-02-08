package ru.practicum.event.service;

import jakarta.servlet.http.HttpServletRequest;
import ru.practicum.api.event.dto.EventFullDto;
import ru.practicum.api.event.dto.EventShortDto;
import ru.practicum.api.event.service.EventServiceApi;
import ru.practicum.event.dto.EventParams;

import java.util.List;

public interface PublicEventService extends EventServiceApi {

    List<EventShortDto> getAllByParams(EventParams eventParams, HttpServletRequest request);

    EventFullDto getById(long userId, long eventId);

    List<EventShortDto> getRecommendationsForUser(long userId);

    void likeEvent(long userId, long eventId);
}
