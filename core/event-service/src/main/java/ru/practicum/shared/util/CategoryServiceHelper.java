package ru.practicum.shared.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.api.category.dto.ResponseCategoryDto;
import ru.practicum.category.dao.CategoryRepository;
import ru.practicum.category.mapper.CategoryMapper;
import ru.practicum.shared.error.exception.NotFoundException;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceHelper {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public ResponseCategoryDto getResponseCategoryDto(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .map(categoryMapper::toCategoryDto)
                .orElseThrow(() -> new NotFoundException("Category with id=" + categoryId + " was not found"));
    }

    public Map<Long, ResponseCategoryDto> getResponseCategoryDtoMap(Set<Long> categoriesIds) {
        return categoryRepository.findAllById(categoriesIds).stream()
                .map(categoryMapper::toCategoryDto)
                .collect(Collectors.toMap(ResponseCategoryDto::getId, Function.identity()));
    }
}
