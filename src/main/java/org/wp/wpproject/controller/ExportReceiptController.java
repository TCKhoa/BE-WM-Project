package org.wp.wpproject.controller;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.wp.wpproject.dto.ExportReceiptDetailRequestDTO;
import org.wp.wpproject.dto.ExportReceiptResponseDTO;
import org.wp.wpproject.entity.ExportReceipt;
import org.wp.wpproject.service.ExportReceiptService;

import jakarta.validation.Valid;   // ✅ Sửa lại import
import java.util.List;
import java.util.Optional;

/**
 * Controller quản lý phiếu xuất kho (ExportReceipt).
 * Cung cấp các API CRUD + soft delete.
 */
@RestController
@RequestMapping("/api/export-receipts")
@AllArgsConstructor
public class ExportReceiptController {

    private final ExportReceiptService exportReceiptService;

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

    // ===================== TẠO MỚI (nhận chi tiết) =====================
    @PostMapping
    public ResponseEntity<ExportReceiptResponseDTO> create(
            @Valid @RequestBody CreateExportReceiptRequest request
    ) {
        ExportReceiptResponseDTO created =
                exportReceiptService.create(request.getExportReceipt(), request.getDetails());
        return ResponseEntity.ok(created);
    }

    // ===================== CẬP NHẬT =====================
    @PutMapping("/{id}")
    public ResponseEntity<ExportReceiptResponseDTO> update(
            @PathVariable String id,
            @Valid @RequestBody ExportReceipt exportReceipt
    ) {
        Optional<ExportReceiptResponseDTO> updated = exportReceiptService.update(id, exportReceipt);
        return updated.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ===================== XÓA MỀM =====================
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDelete(@PathVariable String id) {
        boolean deleted = exportReceiptService.softDelete(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    // ===================== DTO Request cho create =====================
    @Data
    public static class CreateExportReceiptRequest {
        private ExportReceipt exportReceipt;
        private List<ExportReceiptDetailRequestDTO> details;
    }
}
