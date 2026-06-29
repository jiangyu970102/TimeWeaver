package com.timeweaver.controller;

import com.timeweaver.common.utils.ResponseResult;
import com.timeweaver.entity.Category;
import com.timeweaver.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseResult<List<Category>> list(@AuthenticationPrincipal Long userId) {
        return ResponseResult.success(categoryService.getAvailableCategories(userId));
    }

    @GetMapping("/{id}")
    public ResponseResult<Category> getById(@PathVariable Long id) {
        return ResponseResult.success(categoryService.getById(id));
    }

    @PostMapping
    public ResponseResult<Category> create(@AuthenticationPrincipal Long userId,
                                           @RequestBody Category request) {
        return ResponseResult.created(categoryService.create(userId, request));
    }

    @PutMapping("/{id}")
    public ResponseResult<Void> update(@AuthenticationPrincipal Long userId,
                                       @PathVariable Long id,
                                       @RequestBody Category request) {
        categoryService.update(id, userId, request);
        return ResponseResult.success();
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Void> delete(@AuthenticationPrincipal Long userId,
                                       @PathVariable Long id) {
        categoryService.delete(id, userId);
        return ResponseResult.success();
    }
}
