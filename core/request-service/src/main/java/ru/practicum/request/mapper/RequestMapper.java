package ru.practicum.request.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.api.request.dto.RequestDto;
import ru.practicum.request.model.Request;

@Mapper(componentModel = "spring")
public interface RequestMapper {

    @Mapping(source = "id", target = "id")
    @Mapping(source = "request.eventId", target = "event")
    @Mapping(source = "request.requesterId", target = "requester")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "created", target = "created")
    RequestDto toRequestDto(Request request);

}
