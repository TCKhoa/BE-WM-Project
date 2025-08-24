package org.wp.wpproject.repository;

import org.wp.wpproject.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Integer> {
    // Lấy danh sách category chưa bị xóa (deletedAt = null)
    List<Category> findByDeletedAtIsNull();

    // Lấy category theo id khi chưa bị xóa
    Optional<Category> findByIdAndDeletedAtIsNull(Integer id);
}
