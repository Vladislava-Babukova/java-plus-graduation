package ru.practicum.category.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.practicum.api.category.dto.ResponseCategoryDto;
import ru.practicum.category.dto.RequestCategoryDto;
import ru.practicum.category.model.Category;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CategoryMapper {

    ResponseCategoryDto toCategoryDto(Category category);

    @Mapping(target = "id", ignore = true)
    Category toCategory(RequestCategoryDto categoryDto);

    @Mapping(target = "id", ignore = true)
    void updateCategoryFromDto(RequestCategoryDto categoryDto, @MappingTarget Category category);

}
