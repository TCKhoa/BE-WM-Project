package org.wp.wpproject.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.wp.wpproject.entity.Category;
import org.wp.wpproject.repository.CategoryRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    // Lấy danh sách category chưa bị xóa
    public List<Category> getAllActiveCategories() {
        return categoryRepository.findByDeletedAtIsNull();
    }

    // Lấy category theo id (chỉ khi chưa bị xóa)
    public Optional<Category> getCategoryById(Integer id) {
        return categoryRepository.findByIdAndDeletedAtIsNull(id);
    }

    // Tạo mới category
    public Category createCategory(Category category) {
        return categoryRepository.save(category);
    }

    // Cập nhật category
    public Optional<Category> updateCategory(Integer id, Category categoryDetails) {
        Optional<Category> categoryOpt = categoryRepository.findByIdAndDeletedAtIsNull(id);
        if (categoryOpt.isEmpty()) {
            return Optional.empty();
        }
        Category category = categoryOpt.get();
        category.setName(categoryDetails.getName());
        category.setSlug(categoryDetails.getSlug());
        category.setUpdatedAt(LocalDateTime.now());
        Category updatedCategory = categoryRepository.save(category);
        return Optional.of(updatedCategory);
    }

    // Xóa mềm category (set deletedAt)
    public boolean softDeleteCategory(Integer id) {
        Optional<Category> categoryOpt = categoryRepository.findByIdAndDeletedAtIsNull(id);
        if (categoryOpt.isEmpty()) {
            return false;
        }
        Category category = categoryOpt.get();
        category.setDeletedAt(LocalDateTime.now());
        categoryRepository.save(category);
        return true;
    }
}
