package org.wp.wpproject.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.wp.wpproject.dto.ExportReceiptDetailRequestDTO;
import org.wp.wpproject.dto.ExportReceiptDetailResponseDTO;
import org.wp.wpproject.service.ExportReceiptDetailService;

import java.util.List;

@RestController
@RequestMapping("/api/export-receipt-details")
@RequiredArgsConstructor
public class ExportReceiptDetailController {

    private final ExportReceiptDetailService exportReceiptDetailService;

    // Lấy tất cả
    @GetMapping
    public ResponseEntity<List<ExportReceiptDetailResponseDTO>> getAll() {
        return ResponseEntity.ok(exportReceiptDetailService.getAll());
    }

    // Lấy theo ID
    @GetMapping("/{id}")
    public ResponseEntity<ExportReceiptDetailResponseDTO> getById(@PathVariable String id) {
        return exportReceiptDetailService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Lấy theo exportReceiptId
    @GetMapping("/receipt/{receiptId}")
    public ResponseEntity<List<ExportReceiptDetailResponseDTO>> getByReceiptId(@PathVariable String receiptId) {
        return ResponseEntity.ok(exportReceiptDetailService.getByReceiptId(receiptId));
    }

    // Thêm mới
    @PostMapping("/receipt/{receiptId}")
    public ResponseEntity<ExportReceiptDetailResponseDTO> create(
            @PathVariable String receiptId,
            @RequestBody ExportReceiptDetailRequestDTO dto) {

        ExportReceiptDetailResponseDTO created = exportReceiptDetailService.create(receiptId, dto);
        return ResponseEntity.ok(created);
    }

    // Xóa mềm
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDelete(@PathVariable String id) {
        boolean deleted = exportReceiptDetailService.softDelete(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
