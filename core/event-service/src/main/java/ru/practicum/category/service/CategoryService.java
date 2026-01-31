package ru.practicum.category.service;

import ru.practicum.api.category.dto.ResponseCategoryDto;
import ru.practicum.api.category.service.CategoryServiceApi;
import ru.practicum.category.dto.RequestCategoryDto;

import java.util.List;

public interface CategoryService extends CategoryServiceApi {

    List<ResponseCategoryDto> getCategories(int from, int size);

    ResponseCategoryDto save(RequestCategoryDto categoryDto);

    ResponseCategoryDto update(long catId, RequestCategoryDto categoryDto);

    void delete(long catId);

}
