package ru.practicum.request.client.user;


import org.springframework.cloud.openfeign.FeignClient;
import ru.practicum.api.user.service.UserServiceApi;
import ru.practicum.request.config.UserClientConfig;

@FeignClient(name = "user-service", configuration = UserClientConfig.class, fallback = UserClientFallback.class)
public interface UserClient extends UserServiceApi {
}

