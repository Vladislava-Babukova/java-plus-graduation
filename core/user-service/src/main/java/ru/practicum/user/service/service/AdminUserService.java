package ru.practicum.user.service.service;


import ru.practicum.api.user.dto.UserDto;
import ru.practicum.user.service.dto.NewUserRequest;
import ru.practicum.user.service.dto.UserShortDto;

import java.util.List;
import java.util.Map;


public interface AdminUserService {
    UserDto create(NewUserRequest userDto);

    List<UserDto> getUsers(List<Long> ids, int from, int size);

    void delete(Long userId);

    UserDto getUser(Long userId);

    UserShortDto getShortDto(Long userId);

    Boolean userExists(Long userId);

    Map<Long, UserShortDto> getUsersShortDtoBatch(List<Long> ids);
}
