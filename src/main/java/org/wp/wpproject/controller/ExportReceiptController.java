package org.wp.wpproject.controller;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.wp.wpproject.dto.ExportReceiptDetailRequestDTO;
import org.wp.wpproject.dto.ExportReceiptResponseDTO;
import org.wp.wpproject.entity.ExportReceipt;
import org.wp.wpproject.entity.User;
import org.wp.wpproject.service.ExportReceiptService;
import org.wp.wpproject.service.HistoryLogService;
import org.wp.wpproject.service.UserService;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/export-receipts")
@AllArgsConstructor
public class ExportReceiptController {

    private final ExportReceiptService exportReceiptService;
    private final HistoryLogService historyLogService;
    private final UserService userService;

    // ===================== LẤY TẤT CẢ =====================
    @GetMapping
    public ResponseEntity<List<ExportReceiptResponseDTO>> getAll() {
        List<ExportReceiptResponseDTO> receipts = exportReceiptService.getAllDTO();
        return ResponseEntity.ok(receipts);
    }

    // ===================== LẤY THEO ID =====================
    @GetMapping("/{id}")
    public ResponseEntity<ExportReceiptResponseDTO> getById(@PathVariable String id) {
        Optional<ExportReceiptResponseDTO> exportReceipt = exportReceiptService.getByIdDTO(id);
        return exportReceipt.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ===================== TẠO MỚI =====================
    @PostMapping
    public ResponseEntity<ExportReceiptResponseDTO> create(
            @Valid @RequestBody CreateExportReceiptRequest request
    ) {
        User currentUser = userService.getCurrentUser();
        ExportReceipt exportReceipt = request.getExportReceipt();
//        exportReceipt.setCreatedBy(currentUser); // gán người tạo từ token

        ExportReceiptResponseDTO created =
                exportReceiptService.create(exportReceipt, request.getDetails());

        historyLogService.logAction(
                "Tạo phiếu xuất " + created.getExportCode(),
                currentUser
        );

        return ResponseEntity.ok(created);
    }

    // ===================== CẬP NHẬT =====================
    @PutMapping("/{id}")
    public ResponseEntity<ExportReceiptResponseDTO> update(
            @PathVariable String id,
            @Valid @RequestBody ExportReceipt exportReceipt
    ) {
        User currentUser = userService.getCurrentUser();

        Optional<ExportReceiptResponseDTO> updated = exportReceiptService.update(id, exportReceipt);

        updated.ifPresent(dto -> {
            historyLogService.logAction(
                    "Cập nhật phiếu xuất " + dto.getExportCode(),
                    currentUser
            );
        });

        return updated.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ===================== XÓA MỀM (có phân quyền) =====================
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDelete(@PathVariable String id) {
        User currentUser = userService.getCurrentUser();

        // Lấy phiếu trước khi xóa để lấy exportCode
        Optional<ExportReceiptResponseDTO> receiptOpt = exportReceiptService.getByIdDTO(id);

        boolean deleted;
        try {
            deleted = exportReceiptService.softDelete(id, currentUser);
        } catch (RuntimeException ex) {
            // Nếu không có quyền xóa
            return ResponseEntity.status(403).build();
        }

        if (deleted && receiptOpt.isPresent()) {
            String exportCode = receiptOpt.get().getExportCode();
            historyLogService.logAction(
                    "Xóa phiếu xuất " + exportCode,
                    currentUser
            );
        }

        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    // ===================== DTO Request cho create =====================
    @Data
    public static class CreateExportReceiptRequest {
        private ExportReceipt exportReceipt;
        private List<ExportReceiptDetailRequestDTO> details;
    }
}
