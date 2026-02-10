package com.easybill.service;

import com.easybill.dto.CategoryCreateRequest;
import com.easybill.dto.CategoryDTO;
import com.easybill.entity.Category;
import com.easybill.repository.CategoryRepository;
import com.easybill.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;

    /**
     * 获取用户的所有分类（包括系统预置）
     */
    public List<CategoryDTO> getAllCategories(Long userId) {
        List<Category> categories = categoryRepository.findByUserIdOrUserIdIsNullOrderBySortOrder(userId);
        return categories.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 根据类型获取分类
     */
    public List<CategoryDTO> getCategoriesByType(Long userId, String type) {
        List<Category> categories = categoryRepository.findByUserIdAndType(userId, type);
        return categories.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 获取分类详情
     */
    public CategoryDTO getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        return convertToDTO(category);
    }

    /**
     * 创建分类
     */
    @Transactional
    public CategoryDTO createCategory(Long userId, CategoryCreateRequest request) {
        Category category = new Category();
        category.setUserId(userId);
        category.setName(request.getName());
        category.setType(request.getType());
        category.setIcon(request.getIcon());
        category.setParentId(request.getParentId());
        category.setIsSystem(false);
        
        Category saved = categoryRepository.save(category);
        return convertToDTO(saved);
    }

    /**
     * 更新分类
     */
    @Transactional
    public CategoryDTO updateCategory(Long id, Long userId, CategoryCreateRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        
        // 验证权限（系统分类不能修改）
        if (category.getIsSystem() != null && category.getIsSystem()) {
            throw new RuntimeException("Cannot modify system category");
        }
        
        if (category.getUserId() != null && !category.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized access");
        }
        
        // 更新字段
        if (request.getName() != null) {
            category.setName(request.getName());
        }
        if (request.getType() != null) {
            category.setType(request.getType());
        }
        if (request.getIcon() != null) {
            category.setIcon(request.getIcon());
        }
        if (request.getParentId() != null) {
            category.setParentId(request.getParentId());
        }
        
        Category saved = categoryRepository.save(category);
        return convertToDTO(saved);
    }

    /**
     * 删除分类
     */
    @Transactional
    public void deleteCategory(Long id, Long userId) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        
        // 验证权限
        if (category.getIsSystem() != null && category.getIsSystem()) {
            throw new RuntimeException("Cannot delete system category");
        }
        
        if (category.getUserId() != null && !category.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized access");
        }
        
        categoryRepository.delete(category);
    }

    /**
     * 转换为 DTO
     */
    private CategoryDTO convertToDTO(Category category) {
        CategoryDTO dto = CategoryDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .type(category.getType())
                .icon(category.getIcon())
                .parentId(category.getParentId())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
        
        // 获取父分类名称
        if (category.getParentId() != null) {
            categoryRepository.findById(category.getParentId())
                    .ifPresent(parent -> dto.setParentName(parent.getName()));
        }
        
        // 获取交易数量（可选，性能考虑可以按需加载）
        // Long count = transactionRepository.countByCategoryId(category.getId());
        // dto.setTransactionCount(count.intValue());
        
        return dto;
    }
}
