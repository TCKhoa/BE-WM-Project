package org.wp.wpproject.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.wp.wpproject.entity.Brand;

import java.util.List;
import java.util.Optional;

public interface BrandRepository extends JpaRepository<Brand, Integer> {

    // Lấy danh sách brand chưa bị xóa
    List<Brand> findByDeletedAtIsNull();

    // Tìm brand theo id và chưa bị xóa
    Optional<Brand> findByIdAndDeletedAtIsNull(Integer id);
}
