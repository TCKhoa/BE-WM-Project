package org.wp.wpproject.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.wp.wpproject.entity.Category;
import org.wp.wpproject.entity.User;
import org.wp.wpproject.service.CategoryService;
import org.wp.wpproject.service.HistoryLogService;
import org.wp.wpproject.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private HistoryLogService historyLogService;

    @Autowired
    private UserService userService;

    // Lấy danh sách category chưa xóa
    @GetMapping
    public List<Category> getAllActiveCategories() {
        return categoryService.getAllActiveCategories();
    }

    // Lấy category theo id
    @GetMapping("/{id}")
    public ResponseEntity<Category> getCategoryById(@PathVariable Integer id) {
        Optional<Category> categoryOpt = categoryService.getCategoryById(id);
        return categoryOpt.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Tạo mới category
    @PostMapping
    public ResponseEntity<Category> createCategory(@RequestBody Category category) {
        User currentUser = userService.getCurrentUser();

        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());
        category.setDeletedAt(null);  // Mặc định chưa xóa

        Category created = categoryService.createCategory(category);

        // Ghi log
        historyLogService.logAction(
                "Tạo danh mục " + created.getName(),
                currentUser
        );

        return ResponseEntity.ok(created);
    }

    // Cập nhật category
    @PutMapping("/{id}")
    public ResponseEntity<Category> updateCategory(@PathVariable Integer id, @RequestBody Category categoryDetails) {
        User currentUser = userService.getCurrentUser();

        Optional<Category> updatedCategory = categoryService.updateCategory(id, categoryDetails);

        updatedCategory.ifPresent(c -> historyLogService.logAction(
                "Cập nhật danh mục " + c.getName(),
                currentUser
        ));

        return updatedCategory.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Xóa mềm (soft delete) category
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDeleteCategory(@PathVariable Integer id) {
        User currentUser = userService.getCurrentUser();

        Optional<Category> categoryOpt = categoryService.getCategoryById(id);
        boolean deleted = categoryService.softDeleteCategory(id);

        if (deleted && categoryOpt.isPresent()) {
            historyLogService.logAction(
                    "Xóa danh mục " + categoryOpt.get().getName(),
                    currentUser
            );
        }

        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
