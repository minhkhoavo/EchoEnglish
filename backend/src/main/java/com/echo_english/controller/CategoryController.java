package com.echo_english.controller;

import com.echo_english.dto.response.CategoryResponse;
import com.echo_english.entity.Category;
import com.echo_english.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/categories")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    @GetMapping("/{id}")
    public ResponseEntity<Category> getCategoryById(@PathVariable Long id) {
        Category category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(category);
    }

    @GetMapping("/public")
    public ResponseEntity<List<CategoryResponse>> getPublicCategories() {
        List<CategoryResponse> categories = categoryService.getPublicCategories();
        return ResponseEntity.ok(categories);
    }
}