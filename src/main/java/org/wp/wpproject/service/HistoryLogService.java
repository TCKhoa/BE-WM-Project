package org.wp.wpproject.service;

import org.springframework.stereotype.Service;
import org.wp.wpproject.entity.HistoryLog;
import org.wp.wpproject.entity.User;
import org.wp.wpproject.repository.HistoryLogRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class HistoryLogService {

    private final HistoryLogRepository historyLogRepository;

    public HistoryLogService(HistoryLogRepository historyLogRepository) {
        this.historyLogRepository = historyLogRepository;
    }

    // Lấy tất cả log chưa bị xóa
    public List<HistoryLog> getAllLogs() {
        return historyLogRepository.findByDeletedAtIsNull();
    }

    // Lấy log theo ID chưa bị xóa
    public Optional<HistoryLog> getLogById(String id) {
        return historyLogRepository.findByIdAndDeletedAtIsNull(id);
    }

    // Tạo log mới, bắt buộc phải set user liên kết
    public HistoryLog createLog(HistoryLog log, User user) {
        if (log.getId() == null || log.getId().isEmpty()) {
            log.setId(UUID.randomUUID().toString());
        }
        log.setUser(user); // liên kết với user
        log.setPerformedAt(LocalDateTime.now());
        return historyLogRepository.save(log);
    }

    // Xóa mềm log
    public boolean softDeleteLog(String id) {
        Optional<HistoryLog> opt = historyLogRepository.findByIdAndDeletedAtIsNull(id);
        if (opt.isEmpty()) return false;

        HistoryLog log = opt.get();
        log.setDeletedAt(LocalDateTime.now());
        historyLogRepository.save(log);
        return true;
    }

    // Tùy chọn: Lấy log theo user
    public List<HistoryLog> getLogsByUser(User user) {
        return historyLogRepository.findByUserAndDeletedAtIsNull(user);
    }
}
