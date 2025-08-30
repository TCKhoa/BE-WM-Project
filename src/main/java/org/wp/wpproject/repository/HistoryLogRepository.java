package org.wp.wpproject.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.wp.wpproject.entity.HistoryLog;

public interface HistoryLogRepository extends JpaRepository<HistoryLog, String> {
}
