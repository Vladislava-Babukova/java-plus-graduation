package ru.practicum.request.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.api.request.enums.RequestStatus;
import ru.practicum.api.request.service.AdminRequestServiceApi;
import ru.practicum.request.service.AdminRequestService;

import java.util.Map;
import java.util.Set;

@RestController
@Validated
@RequiredArgsConstructor
public class AdminRequestController implements AdminRequestServiceApi {
    private final AdminRequestService adminRequestService;

    @Override
    public Map<Long, Long> getRequestsCountsByStatusAndEventIds(RequestStatus status, Set<Long> eventIds) {
        return adminRequestService.getRequestsCountsByStatusAndEventIds(status, eventIds);
    }
}
