package com.timeweaver.service;

import com.timeweaver.common.constant.ApiCode;
import com.timeweaver.common.exception.BusinessException;
import com.timeweaver.entity.Category;
import com.timeweaver.mapper.CategoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryMapper categoryMapper;

    public List<Category> getAvailableCategories(Long userId) {
        return categoryMapper.findAvailableCategories(userId);
    }

    public Category getById(Long id) {
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException(ApiCode.CATEGORY_NOT_FOUND);
        }
        return category;
    }

    public Category create(Long userId, Category request) {
        Category category = new Category();
        category.setUserId(userId);
        category.setName(request.getName());
        category.setIcon(request.getIcon());
        category.setColor(request.getColor());
        category.setType(request.getType());
        category.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        category.setIsSystem(0);
        categoryMapper.insert(category);
        return category;
    }

    public void update(Long id, Long userId, Category request) {
        Category category = categoryMapper.selectById(id);
        if (category == null || category.getIsSystem() == 1) {
            throw new BusinessException(ApiCode.NOT_FOUND, "分类不存在或为系统预设");
        }
        if (!userId.equals(category.getUserId())) {
            throw new BusinessException(ApiCode.FORBIDDEN);
        }
        category.setName(request.getName());
        category.setIcon(request.getIcon());
        category.setColor(request.getColor());
        category.setType(request.getType());
        category.setSortOrder(request.getSortOrder());
        categoryMapper.updateById(category);
    }

    public void delete(Long id, Long userId) {
        Category category = categoryMapper.selectById(id);
        if (category == null || category.getIsSystem() == 1) {
            throw new BusinessException(ApiCode.NOT_FOUND, "分类不存在或为系统预设");
        }
        if (!userId.equals(category.getUserId())) {
            throw new BusinessException(ApiCode.FORBIDDEN);
        }
        categoryMapper.deleteById(id);
    }
}
