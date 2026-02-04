package ru.practicum.user.service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.api.user.dto.UserDto;
import ru.practicum.user.service.dao.UserRepository;
import ru.practicum.user.service.error.exception.NotFoundException;
import ru.practicum.user.service.mapper.UserMapper;

import java.util.List;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class PrivateUserServiceImpl implements PrivateUserService {

    private final UserRepository userRepository;

    private final UserMapper userMapper;

    @Override
    public UserDto getUserById(Long userId) {
        log.info("Получение пользователя по id: {}", userId);
        return userRepository.findById(userId).map(userMapper::toUserDto)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));
    }

    @Override
    public List<UserDto> getAllByIds(Set<Long> userIds) {
        return userRepository.findAllById(userIds).stream()
                .map(userMapper::toUserDto)
                .toList();
    }
}