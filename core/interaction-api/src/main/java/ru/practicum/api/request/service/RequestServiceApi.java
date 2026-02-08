package ru.practicum.api.request.service;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.practicum.api.request.dto.RequestDto;

import java.util.List;

public interface RequestServiceApi extends AdminRequestServiceApi {

    @GetMapping(path = "/users/{userId}/events/{eventId}/requests", produces = MediaType.APPLICATION_JSON_VALUE)
    List<RequestDto> getEventRequests(
            @PathVariable Long userId,
            @PathVariable Long eventId
    );

}
