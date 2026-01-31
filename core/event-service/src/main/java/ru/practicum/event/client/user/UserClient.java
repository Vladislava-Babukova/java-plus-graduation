package ru.practicum.event.client.user;

import org.springframework.cloud.openfeign.FeignClient;
import ru.practicum.api.user.service.UserServiceApi;
import ru.practicum.event.config.UserClientConfig;


@FeignClient(name = "user-service", configuration = UserClientConfig.class, fallback = UserClientFallback.class)
public interface UserClient extends UserServiceApi {
}
