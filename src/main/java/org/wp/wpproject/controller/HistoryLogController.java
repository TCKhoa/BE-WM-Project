package org.wp.wpproject.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.wp.wpproject.entity.HistoryLog;
import org.wp.wpproject.entity.User;
import org.wp.wpproject.service.HistoryLogService;
import org.wp.wpproject.service.UserService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/history-logs")
@RequiredArgsConstructor
public class HistoryLogController {

    private final HistoryLogService historyLogService;
    private final UserService userService; // để tìm user theo ID

    // Lấy tất cả lịch sử chưa xóa
    @GetMapping
    public ResponseEntity<List<HistoryLog>> getAllLogs() {
        return ResponseEntity.ok(historyLogService.getAllLogs());
    }

    // Lấy 1 log theo ID
    @GetMapping("/{id}")
    public ResponseEntity<HistoryLog> getLogById(@PathVariable String id) {
        Optional<HistoryLog> log = historyLogService.getLogById(id);
        return log.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Thêm log mới
    // Thêm log mới
    @PostMapping
    public ResponseEntity<HistoryLog> createLog(@RequestBody HistoryLog historyLog) {
        if (historyLog.getUser() == null || historyLog.getUser().getId() == null) {
            return ResponseEntity.badRequest().build();
        }

        Optional<User> userOpt = userService.getUserById(historyLog.getUser().getId());
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }

        // Gọi service với cả HistoryLog và User
        HistoryLog createdLog = historyLogService.createLog(historyLog, userOpt.get());
        return ResponseEntity.ok(createdLog);
    }


    // Xóa mềm log
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDeleteLog(@PathVariable String id) {
        boolean deleted = historyLogService.softDeleteLog(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
