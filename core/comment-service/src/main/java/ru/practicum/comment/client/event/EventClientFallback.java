package ru.practicum.comment.client.event;

import io.github.resilience4j.core.lang.Nullable;
import jakarta.validation.constraints.Positive;
import jakarta.ws.rs.ServiceUnavailableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.api.event.dto.EventFullDto;
import ru.practicum.api.event.dto.EventShortDto;
import ru.practicum.api.event.enums.EventState;

import java.util.List;
import java.util.Set;

@Slf4j
@Component
public class EventClientFallback implements EventClient {

    @Override
    public EventFullDto getByIdAndState(Long eventId, @Nullable EventState state) {
        log.warn("Сервис Event недоступен, fallback кинул ServiceUnavailableException для eventId: {} и state: {}", eventId, state);
        throw new ServiceUnavailableException("Сервис Event недоступен");
    }

    @Override
    public List<EventShortDto> getAllByIds(Set<@Positive Long> eventIds) {
        log.warn("Сервис Event недоступен, fallback вернул пустой список для eventIds: {}", eventIds);
        return List.of();
    }

}

