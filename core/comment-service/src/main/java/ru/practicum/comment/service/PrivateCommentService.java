package ru.practicum.comment.service;

import ru.practicum.comment.dto.NewCommentDto;
import ru.practicum.comment.dto.ResponseCommentDto;

public interface PrivateCommentService {
    ResponseCommentDto create(Long userId, Long eventId, NewCommentDto dto);

    ResponseCommentDto patch(Long userId, Long eventId, Long commentId, NewCommentDto dto);

    void delete(Long userId, Long eventId, Long commentId);
}
