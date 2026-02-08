package ru.practicum.event.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.api.category.dto.ResponseCategoryDto;
import ru.practicum.api.event.dto.EventFullDto;
import ru.practicum.api.event.dto.EventShortDto;
import ru.practicum.api.event.enums.EventState;
import ru.practicum.api.request.enums.RequestStatus;
import ru.practicum.api.user.dto.UserShortDto;
import ru.practicum.client.UserActionClient;
import ru.practicum.event.client.request.RequestClient;
import ru.practicum.event.client.user.UserClient;
import ru.practicum.event.dao.EventRepository;
import ru.practicum.event.dao.EventSpecifications;
import ru.practicum.event.dto.EventParams;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.mapper.UserMapper;
import ru.practicum.event.model.Event;
import ru.practicum.shared.error.exception.BadRequestException;
import ru.practicum.shared.error.exception.LikeValidationException;
import ru.practicum.shared.error.exception.NotFoundException;
import ru.practicum.shared.util.CategoryServiceHelper;
import ru.practicum.shared.util.EventServiceHelper;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PublicEventServiceImpl implements PublicEventService {

    private final EventRepository eventRepository;
    private final UserClient userClient;
    private final RequestClient requestClient;
    private final UserActionClient userActionClient;
    private final CategoryServiceHelper categoryServiceHelper;
    private final EventServiceHelper eventServiceHelper;
    private final EventMapper eventMapper;
    private final UserMapper userMapper;

    @Override
    public List<EventShortDto> getAllByParams(EventParams params, HttpServletRequest request) {
        log.info("Получение событий с параметрами: {}", params.toString());

        if (params.getRangeStart() != null && params.getRangeEnd() != null && params.getRangeEnd().isBefore(params.getRangeStart())) {
            log.error("Ошибка в параметрах диапазона дат: start={}, end={}", params.getRangeStart(), params.getRangeEnd());
            throw new BadRequestException("Дата начала должна быть раньше даты окончания");
        }

        if (params.getRangeStart() == null) params.setRangeStart(LocalDateTime.now());

        List<Event> events = eventRepository
                .findAll(EventSpecifications.publicSpecification(params), makePageable(params))
                .stream()
                .toList();

        Set<Long> eventIds = events.stream().map(Event::getId).collect(Collectors.toSet());

        Map<Long, Long> confirmedRequests = requestClient.getRequestsCountsByStatusAndEventIds(RequestStatus.CONFIRMED, eventIds);

        if (params.getOnlyAvailable()) {
            events = events.stream()
                    .filter(event -> event.getParticipantLimit() > confirmedRequests.get(event.getId()))
                    .toList();
        }

        if (events.isEmpty()) {
            log.warn("Нет свободных событий по указанным параметрам {}", params);
            return Collections.emptyList();
        }

        Map<Long, Double> ratings = eventServiceHelper.getRatingsMap(eventIds);

        Set<Long> userIds = events.stream()
                .map(Event::getInitiatorId)
                .collect(Collectors.toSet());
        Set<Long> categoriesIds = new HashSet<>(params.getCategories());

        Map<Long, UserShortDto> userShortDtos = eventServiceHelper.getUserShortDtoMap(userIds);

        Map<Long, ResponseCategoryDto> categoryDtos = categoryServiceHelper.getResponseCategoryDtoMap(categoriesIds);

        return eventServiceHelper.getEventShortDtos(userShortDtos, categoryDtos, events, confirmedRequests, ratings, eventMapper);
    }

    @Override
    public EventFullDto getById(long userId, long eventId) {
        log.debug("Получение события с ID = {}", eventId);

        Event event = eventRepository.findByIdAndState(eventId, EventState.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Событие не найдено."));

        Long confirmedRequests = requestClient.getRequestsCountsByStatusAndEventIds(RequestStatus.CONFIRMED, Set.of(eventId)).getOrDefault(eventId, 0L);

        userActionClient.sendViewEvent(userId, eventId);

        ResponseCategoryDto categoryDto = categoryServiceHelper
                .getResponseCategoryDto(event.getCategoryId());

        UserShortDto userShortDto = userMapper.toUserShortDto(userClient.getUserById(event.getInitiatorId()));

        if (event.getPublishedOn() == null) {
            return eventMapper.toEventFullDto(event, categoryDto, userShortDto, confirmedRequests, 0.0);
        }

        double rating = eventServiceHelper.getRatingsMap(Set.of(event.getId()))
                .getOrDefault(event.getId(), 0.0);

        EventFullDto dto = eventMapper.toEventFullDto(event, categoryDto, userShortDto, confirmedRequests, rating);

        log.debug("Получено событие с ID={}: {}", eventId, dto);

        return dto;
    }

    @Override
    public EventFullDto getByIdAndState(Long eventId, EventState state) {
        log.debug("Получен запрос на получение события с ID = {} и state = {}", eventId, state);

        Event event;
        if (state == null) {
            event = eventRepository.findById(eventId)
                    .orElseThrow(() -> new NotFoundException("Событие c ID = " + eventId + " не найдено"));
        } else {
            event = eventRepository.findByIdAndState(eventId, state)
                    .orElseThrow(() -> new NotFoundException("Событие c ID = " + eventId + " не найдено"));
        }

        Long confirmedRequests = requestClient.getRequestsCountsByStatusAndEventIds(RequestStatus.CONFIRMED, Set.of(eventId)).getOrDefault(eventId, 0L);

        ResponseCategoryDto categoryDto = categoryServiceHelper
                .getResponseCategoryDto(event.getCategoryId());

        UserShortDto userDto = userMapper.toUserShortDto(userClient.getUserById(event.getInitiatorId()));

        if (event.getPublishedOn() == null) {
            return eventMapper.toEventFullDto(event, categoryDto, userDto, confirmedRequests, 0.0);
        }

        double rating = eventServiceHelper.getRatingsMap(Set.of(event.getId()))
                .getOrDefault(event.getId(), 0.0);

        EventFullDto dto = eventMapper.toEventFullDto(event, categoryDto, userDto, confirmedRequests, rating);

        log.debug("Получено событие с ID={}: {}", eventId, dto);

        return dto;
    }

    @Override
    public List<EventShortDto> getAllByIds(Set<Long> eventIds) {
        log.debug("Получен запрос на получение событий с IDs = {}", eventIds);

        List<Event> events = eventRepository.findAllById(eventIds);

        if (events.isEmpty()) {
            log.warn("Нет событий по указанным IDs {}", eventIds);
            return Collections.emptyList();
        }

        Set<Long> dbEventIds = events.stream()
                .map(Event::getId)
                .collect(Collectors.toSet());

        Map<Long, Long> confirmedRequests = requestClient.getRequestsCountsByStatusAndEventIds(RequestStatus.CONFIRMED, dbEventIds);

        Map<Long, Double> ratings = eventServiceHelper.getRatingsMap(dbEventIds);

        Set<Long> userIds = events.stream()
                .map(Event::getInitiatorId)
                .collect(Collectors.toSet());
        Set<Long> categoryIds = events.stream()
                .map(Event::getInitiatorId)
                .collect(Collectors.toSet());

        Map<Long, UserShortDto> userShortDtos = eventServiceHelper.getUserShortDtoMap(userIds);

        Map<Long, ResponseCategoryDto> categoryDtos = categoryServiceHelper
                .getResponseCategoryDtoMap(categoryIds);

        return eventServiceHelper.getEventShortDtos(userShortDtos, categoryDtos, events, confirmedRequests, ratings, eventMapper);
    }

    @Override
    public List<EventShortDto> getRecommendationsForUser(long userId) {
        log.debug("Получен запрос на получение рекомендаций для пользователя с ID = {}", userId);

        Map<Long, Double> recommendations = eventServiceHelper.getRecommendationsMap(userId);

        if (recommendations.isEmpty()) {
            log.warn("Не нашлось рекомендаций для пользователя с ID = {}", userId);
            return Collections.emptyList();
        }

        log.debug("Рекомендации для пользователя с ID = {}, {}", userId, recommendations);

        return getAllByIds(recommendations.keySet());
    }

    @Override
    public void likeEvent(long userId, long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие c ID = " + eventId + " не найдено"));

        if (event.getRequestModeration() || event.getParticipantLimit() != 0) {
            if (event.getInitiatorId() != userId && requestClient.getEventRequests(userId, eventId).stream()
                    .filter(o -> o.getStatus().equals(RequestStatus.CONFIRMED.name())).toList().isEmpty()) {
                throw new LikeValidationException("Пользователь = " + userId + " не может лайкать мероприятие = " + eventId);
            }
        }

        userActionClient.sendLikeEvent(userId, eventId);
    }

    private Pageable makePageable(EventParams params) {
        Sort sort = params.getEventsSort().getSort();
        return PageRequest.of(params.getFrom() / params.getSize(), params.getSize(), sort);
    }

}