package ru.practicum.user.service.service;


import ru.practicum.api.user.dto.UserDto;
import ru.practicum.user.service.dto.NewUserRequest;

import java.util.List;


public interface AdminUserService {
    UserDto create(NewUserRequest userDto);

    List<UserDto> getUsers(List<Long> ids, int from, int size);

    void delete(Long userId);
}
