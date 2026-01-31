package ru.practicum.comment.service;

import ru.practicum.comment.dto.ResponseCommentDto;
import ru.practicum.comment.dto.UpdateCommentDto;
import ru.practicum.comment.enums.Status;

import java.util.List;

public interface AdminCommentService {

    List<ResponseCommentDto> getAll(Status status, int from, int size);

    List<ResponseCommentDto> getByEventId(long eventId, Status status);

    void update(long eventId, long commentId, UpdateCommentDto commentDto);

}
