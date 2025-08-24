package org.wp.wpproject.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.wp.wpproject.entity.HistoryLog;
import org.wp.wpproject.entity.User;

import java.util.List;
import java.util.Optional;

public interface HistoryLogRepository extends JpaRepository<HistoryLog, String> {

    // Lấy tất cả log chưa bị xóa
    List<HistoryLog> findByDeletedAtIsNull();

    // Lấy log theo ID, chưa bị xóa
    Optional<HistoryLog> findByIdAndDeletedAtIsNull(String id);

    // Tùy chọn: Lấy log theo user
    List<HistoryLog> findByUserAndDeletedAtIsNull(User user);
}
