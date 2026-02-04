package ru.practicum.event.client.request;

import org.springframework.cloud.openfeign.FeignClient;
import ru.practicum.api.request.service.AdminRequestServiceApi;
import ru.practicum.event.config.RequestClientConfig;


@FeignClient(name = "request-service", configuration = RequestClientConfig.class, fallback = RequestClientFallback.class)
public interface RequestClient extends AdminRequestServiceApi {
}
