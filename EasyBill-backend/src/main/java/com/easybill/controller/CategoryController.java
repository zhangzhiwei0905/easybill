package com.easybill.controller;

import com.easybill.dto.CategoryCreateRequest;
import com.easybill.dto.CategoryDTO;
import com.easybill.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * 获取所有分类
     */
    @GetMapping
    public ResponseEntity<List<CategoryDTO>> getAllCategories(
            @RequestParam(required = false) String type
    ) {
        Long userId = getCurrentUserId();
        
        List<CategoryDTO> categories;
        if (type != null) {
            categories = categoryService.getCategoriesByType(userId, type);
        } else {
            categories = categoryService.getAllCategories(userId);
        }
        
        return ResponseEntity.ok(categories);
    }

    /**
     * 获取分类详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<CategoryDTO> getCategory(@PathVariable Long id) {
        CategoryDTO category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(category);
    }

    /**
     * 创建分类
     */
    @PostMapping
    public ResponseEntity<CategoryDTO> createCategory(@RequestBody CategoryCreateRequest request) {
        Long userId = getCurrentUserId();
        CategoryDTO created = categoryService.createCategory(userId, request);
        return ResponseEntity.ok(created);
    }

    /**
     * 更新分类
     */
    @PutMapping("/{id}")
    public ResponseEntity<CategoryDTO> updateCategory(
            @PathVariable Long id,
            @RequestBody CategoryCreateRequest request
    ) {
        Long userId = getCurrentUserId();
        CategoryDTO updated = categoryService.updateCategory(id, userId, request);
        return ResponseEntity.ok(updated);
    }

    /**
     * 删除分类
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteCategory(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        categoryService.deleteCategory(id, userId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "分类已删除"
        ));
    }

    /**
     * 获取当前用户ID
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return 1L; // 暂时返回固定值
    }
}
