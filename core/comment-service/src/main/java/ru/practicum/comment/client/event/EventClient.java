package ru.practicum.comment.client.event;

import org.springframework.cloud.openfeign.FeignClient;
import ru.practicum.api.event.service.EventServiceApi;
import ru.practicum.comment.config.EventClientConfig;

@FeignClient(name = "event-service", configuration = EventClientConfig.class, fallback = EventClientFallback.class)
public interface EventClient extends EventServiceApi {
}