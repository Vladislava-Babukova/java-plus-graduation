package ru.practicum.user.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.api.user.dto.UserDto;
import ru.practicum.user.service.dto.NewUserRequest;
import ru.practicum.user.service.dto.UserShortDto;
import ru.practicum.user.service.model.User;


@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    User toUser(NewUserRequest newUserRequest);

    UserDto toUserDto(User user);

    UserShortDto toUserShortDto(User user);

}
