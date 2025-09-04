package org.wp.wpproject.repository;

import org.wp.wpproject.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {

    // --- Lấy tất cả sản phẩm của một location chưa bị xóa ---
    List<Product> findByLocationIdAndDeletedAtIsNull(Integer locationId);

    // --- Lấy tất cả sản phẩm đã bị xóa trước một thời điểm ---
    List<Product> findByDeletedAtBefore(LocalDateTime cutoffDate);

    // --- Xóa vĩnh viễn sản phẩm đã bị xóa trước một thời điểm ---
    @Modifying
    @Query("DELETE FROM Product p WHERE p.deletedAt < :cutoffDate")
    void deleteOldProductsPermanently(LocalDateTime cutoffDate);
}
