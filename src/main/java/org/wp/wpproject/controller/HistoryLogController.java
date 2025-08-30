package org.wp.wpproject.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.wp.wpproject.dto.HistoryLogDTO;
import org.wp.wpproject.entity.HistoryLog;
import org.wp.wpproject.entity.User;
import org.wp.wpproject.service.HistoryLogService;
import org.wp.wpproject.service.UserService;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController
@RequestMapping("/api/history-logs")
@RequiredArgsConstructor
public class HistoryLogController {

    private final HistoryLogService historyLogService;
    private final UserService userService;

    // ------------------ SSE ------------------
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    @GetMapping("/stream")
    public SseEmitter streamHistoryLogs() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        emitters.add(emitter);

        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));

        return emitter;
    }

    private void pushNewLog(HistoryLog log) {
        List<SseEmitter> deadEmitters = new CopyOnWriteArrayList<>();
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("new-log")
                        .data(historyLogService.toDTO(log)));
            } catch (IOException e) {
                deadEmitters.add(emitter);
            }
        }
        emitters.removeAll(deadEmitters);
    }

    // ------------------ CRUD LOGS ------------------

    // --- Lấy tất cả log chưa xóa ---
    @GetMapping
    public ResponseEntity<List<HistoryLogDTO>> getAllLogs() {
        List<HistoryLogDTO> dtos = historyLogService.getAllLogs()
                .stream()
                .map(historyLogService::toDTO)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    // --- Lấy tất cả log chưa đọc ---
    @GetMapping("/unread")
    public ResponseEntity<List<HistoryLogDTO>> getUnreadLogs() {
        List<HistoryLogDTO> dtos = historyLogService.getUnreadLogs()
                .stream()
                .map(historyLogService::toDTO)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    // --- Lấy log theo ID ---
    @GetMapping("/{id}")
    public ResponseEntity<HistoryLogDTO> getLogById(@PathVariable String id) {
        return historyLogService.getLogById(id)
                .map(historyLogService::toDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // --- Tạo log mới ---
    @PostMapping
    public ResponseEntity<HistoryLogDTO> createLog(@RequestBody HistoryLogDTO dto) {
        if (dto.getUserId() == null) {
            return ResponseEntity.badRequest().build();
        }

        Optional<User> userOpt = userService.getUserById(dto.getUserId());
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        HistoryLog entity = HistoryLog.builder()
                .action(dto.getAction())
                .status(dto.getStatus() != null ? dto.getStatus() : "SUCCESS")
                .user(userOpt.get())
                .performedAt(dto.getPerformedAt() != null ? dto.getPerformedAt() : LocalDateTime.now())
                .isRead(dto.getIsRead() != null ? dto.getIsRead() : false)
                .build();

        HistoryLog createdLog = historyLogService.createLog(entity, userOpt.get());

        // Gửi thông báo SSE ngay lập tức
        pushNewLog(createdLog);

        return ResponseEntity.ok(historyLogService.toDTO(createdLog));
    }

    // --- Xóa mềm log ---
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDeleteLog(@PathVariable String id) {
        boolean deleted = historyLogService.softDeleteLog(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    // --- Đánh dấu log là đã đọc ---
    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markLogAsRead(@PathVariable String id) {
        try {
            boolean updated = historyLogService.markAsRead(id);
            if (!updated) {
                System.out.println("Log not found for id: " + id);
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    // --- Đánh dấu log là chưa đọc ---
    @PatchMapping("/{id}/unread")
    public ResponseEntity<Void> markLogAsUnread(@PathVariable String id) {
        boolean updated = historyLogService.markAsUnread(id);
        return updated ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
