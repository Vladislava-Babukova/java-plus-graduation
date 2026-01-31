package ru.practicum.compilation.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.practicum.api.event.dto.EventShortDto;
import ru.practicum.compilation.dto.CreateCompilationDto;
import ru.practicum.compilation.dto.ResponseCompilationDto;
import ru.practicum.compilation.dto.UpdateCompilationDto;
import ru.practicum.compilation.model.Compilation;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CompilationMapper {

    @Mapping(target = "events", source = "events")
    ResponseCompilationDto toCompilationDto(Compilation compilation, List<EventShortDto> events);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "eventIds", ignore = true)
    @Mapping(target = "pinned", source = "pinned", defaultValue = "false")
    Compilation toCompilation(CreateCompilationDto requestCompilationDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "eventIds", ignore = true)
    void updateCompilationFromDto(UpdateCompilationDto updateCompilationDto, @MappingTarget Compilation compilation);

}
