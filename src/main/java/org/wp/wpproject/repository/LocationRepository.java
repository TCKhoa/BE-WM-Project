package org.wp.wpproject.repository;

import org.wp.wpproject.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface LocationRepository extends JpaRepository<Location, Integer> {
    // Lấy danh sách location chưa bị xóa (deletedAt = null)
    List<Location> findByDeletedAtIsNull();

    // Lấy location theo id mà chưa bị xóa
    Optional<Location> findByIdAndDeletedAtIsNull(Integer id);
}
