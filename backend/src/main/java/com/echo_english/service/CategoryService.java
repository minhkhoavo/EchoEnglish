package com.echo_english.service;

import com.echo_english.dto.response.CategoryResponse;
import com.echo_english.entity.Category;
import com.echo_english.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {
    @Autowired
    private CategoryRepository categoryRepository;

    private static final Long USER_DEFINED_CATEGORY_ID = 1L;

    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getPublicCategories() {
        List<Category> categories = categoryRepository.findByIdNot(USER_DEFINED_CATEGORY_ID);
        return categories.stream()
                .map(this::mapToCategoryResponse)
                .collect(Collectors.toList());
    }

    private CategoryResponse mapToCategoryResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                // Map các trường khác nếu có trong DTO
                .build();
    }
}

