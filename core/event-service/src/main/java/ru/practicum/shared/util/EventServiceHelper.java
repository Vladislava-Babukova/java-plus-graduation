package ru.practicum.shared.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.api.category.dto.ResponseCategoryDto;
import ru.practicum.api.event.dto.EventFullDto;
import ru.practicum.api.event.dto.EventShortDto;
import ru.practicum.api.user.dto.UserDto;
import ru.practicum.api.user.dto.UserShortDto;
import ru.practicum.client.RecommendationsClient;
import ru.practicum.event.client.user.UserClient;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.mapper.UserMapper;
import ru.practicum.event.model.Event;
import ru.practicum.ewm.stats.proto.RecommendedEventProto;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventServiceHelper {

    private final RecommendationsClient recommendationsClient;
    private final UserClient userClient;
    private final UserMapper userMapper;

    public static final int MAX_RESULTS = 5;

    // 1. Убираем RecommendationsClient из параметров
    public Map<Long, Double> getRatingsMap(Set<Long> eventIds) {
        return recommendationsClient.getInteractionsCount(eventIds, MAX_RESULTS)
                .collect(Collectors.toMap(RecommendedEventProto::getEventId, RecommendedEventProto::getScore));
    }

    // 2. Перегруженный метод для одного ID (удобно)
    public double getRatingForEvent(Long eventId) {
        return getRatingsMap(Set.of(eventId)).getOrDefault(eventId, 0.0);
    }

    // 3. Убираем UserClient и UserMapper из параметров
    public Map<Long, UserShortDto> getUserShortDtoMap(Set<Long> userIds) {
        return userClient.getAllByIds(userIds).stream()
                .collect(Collectors.toMap(UserDto::getId, userMapper::toUserShortDto));
    }

    // 4. Остальные методы остаются почти без изменений (без static)
    public List<EventFullDto> getEventFullDtos(
            Map<Long, UserShortDto> userShortDtos,
            Map<Long, ResponseCategoryDto> categoryDtos,
            List<Event> events,
            Map<Long, Long> confirmedRequests,
            Map<Long, Double> ratings,
            EventMapper eventMapper
    ) {
        return events.stream()
                .map(event ->
                        eventMapper.toEventFullDto(
                                event,
                                categoryDtos.get(event.getCategoryId()),
                                userShortDtos.get(event.getInitiatorId()),
                                confirmedRequests.getOrDefault(event.getId(), 0L),
                                ratings.getOrDefault(event.getId(), 0.0)
                        )
                )
                .toList();
    }

    public List<EventShortDto> getEventShortDtos(
            Map<Long, UserShortDto> userShortDtos,
            Map<Long, ResponseCategoryDto> categoryDtos,
            List<Event> events,
            Map<Long, Long> confirmedRequests,
            Map<Long, Double> ratings,
            EventMapper eventMapper
    ) {
        return events.stream()
                .map(event ->
                        eventMapper.toEventShortDto(
                                event,
                                categoryDtos.get(event.getCategoryId()),
                                userShortDtos.get(event.getInitiatorId()),
                                confirmedRequests.getOrDefault(event.getId(), 0L),
                                ratings.getOrDefault(event.getId(), 0.0)
                        )
                )
                .toList();
    }

    public Map<Long, Double> getRecommendationsMap(long userId) {
        return recommendationsClient.getRecommendationsForUser(userId, MAX_RESULTS)
                .collect(Collectors.toMap(RecommendedEventProto::getEventId, RecommendedEventProto::getScore));
    }
}