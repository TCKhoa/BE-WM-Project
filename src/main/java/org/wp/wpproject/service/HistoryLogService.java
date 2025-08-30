// src/main/java/org/wp/wpproject/service/HistoryLogService.java
package org.wp.wpproject.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.wp.wpproject.dto.HistoryLogDTO;
import org.wp.wpproject.entity.HistoryLog;
import org.wp.wpproject.entity.User;
import org.wp.wpproject.repository.HistoryLogRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HistoryLogService {

    private final HistoryLogRepository historyLogRepository;

    // Convert entity -> DTO
    public HistoryLogDTO toDTO(HistoryLog log) {
        return HistoryLogDTO.builder()
                .id(log.getId())
                .action(log.getAction())
                .userId(log.getUser() != null ? log.getUser().getId() : null)
                .username(log.getUser() != null ? log.getUser().getUsername() : null)
                .status(log.getStatus())
                .performedAt(log.getPerformedAt())
                .deletedAt(log.getDeletedAt())
                .isRead(log.getIsRead())
                .build();
    }

    // Ghi log với trạng thái mặc định isRead = false
    public void logAction(String action, User user, String status) {
        HistoryLog log = HistoryLog.builder()
                .id(UUID.randomUUID().toString())
                .action(action)
                .user(user)
                .performedAt(LocalDateTime.now())
                .status(status)
                .isRead(false)
                .build();
        historyLogRepository.save(log);
    }

    public void logAction(String action, User user) {
        logAction(action, user, "SUCCESS");
    }

    // Lấy tất cả log chưa xóa
    public List<HistoryLog> getAllLogs() {
        return historyLogRepository.findAll()
                .stream()
                .filter(log -> log.getDeletedAt() == null)
                .toList();
    }

    // Lấy tất cả log chưa đọc
    public List<HistoryLog> getUnreadLogs() {
        return historyLogRepository.findAll()
                .stream()
                .filter(log -> log.getDeletedAt() == null && (log.getIsRead() == null || !log.getIsRead()))
                .toList();
    }

    // Lấy log theo ID
    public Optional<HistoryLog> getLogById(String id) {
        return historyLogRepository.findById(id)
                .filter(log -> log.getDeletedAt() == null);
    }

    // Tạo log mới
    public HistoryLog createLog(HistoryLog historyLog, User user) {
        historyLog.setId(UUID.randomUUID().toString());
        historyLog.setUser(user);
        historyLog.setPerformedAt(LocalDateTime.now());
        if (historyLog.getStatus() == null) historyLog.setStatus("SUCCESS");
        if (historyLog.getIsRead() == null) historyLog.setIsRead(false);
        return historyLogRepository.save(historyLog);
    }

    // Đánh dấu log đã đọc
    public boolean markAsRead(String id) {
        Optional<HistoryLog> logOpt = historyLogRepository.findById(id);
        if (logOpt.isPresent()) {
            HistoryLog log = logOpt.get();
            log.setIsRead(true);
            historyLogRepository.save(log);
            return true;
        }
        return false;
    }

    // Đánh dấu log chưa đọc (mới thêm)
    public boolean markAsUnread(String id) {
        Optional<HistoryLog> logOpt = historyLogRepository.findById(id);
        if (logOpt.isPresent()) {
            HistoryLog log = logOpt.get();
            log.setIsRead(false);
            historyLogRepository.save(log);
            return true;
        }
        return false;
    }

    // Xóa mềm
    public boolean softDeleteLog(String id) {
        Optional<HistoryLog> logOpt = historyLogRepository.findById(id);
        if (logOpt.isPresent()) {
            HistoryLog log = logOpt.get();
            log.setDeletedAt(LocalDateTime.now());
            historyLogRepository.save(log);
            return true;
        }
        return false;
    }
}
