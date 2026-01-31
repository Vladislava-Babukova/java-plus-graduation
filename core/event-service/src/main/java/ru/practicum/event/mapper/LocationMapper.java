package ru.practicum.event.mapper;

import org.mapstruct.Mapper;
import ru.practicum.api.event.dto.LocationDto;
import ru.practicum.event.model.Location;

@Mapper(componentModel = "spring")
public interface LocationMapper {

    Location toEntity(LocationDto dto);

    LocationDto toDto(Location location);

}