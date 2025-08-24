package org.wp.wpproject.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.wp.wpproject.dto.ImportReceiptDetailRequestDTO;
import org.wp.wpproject.dto.ImportReceiptDetailResponseDTO;
import org.wp.wpproject.service.ImportReceiptDetailService;

import java.util.List;

@RestController
@RequestMapping("/api/import-receipt-details")
@RequiredArgsConstructor
public class ImportReceiptDetailController {

    private final ImportReceiptDetailService detailService;

    // ================== LẤY DỮ LIỆU ==================

    @GetMapping
    public ResponseEntity<List<ImportReceiptDetailResponseDTO>> getAllDetails() {
        return ResponseEntity.ok(detailService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ImportReceiptDetailResponseDTO> getDetailById(@PathVariable String id) {
        return detailService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/receipt/{receiptId}")
    public ResponseEntity<List<ImportReceiptDetailResponseDTO>> getByReceiptId(@PathVariable String receiptId) {
        return ResponseEntity.ok(detailService.getByReceiptId(receiptId));
    }

    // ================== TẠO MỚI (cách 2) ==================
    // URL: POST /api/import-receipt-details/{receiptId}
    // Body: { "productId": "...", "quantity": 10, "price": 100000 }
    @PostMapping("/{receiptId}")
    public ResponseEntity<ImportReceiptDetailResponseDTO> createDetail(
            @PathVariable String receiptId,
            @RequestBody ImportReceiptDetailRequestDTO dto
    ) {
        ImportReceiptDetailResponseDTO saved = detailService.create(receiptId, dto);
        return ResponseEntity.ok(saved);
    }

    // ================== XÓA MỀM ==================
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDeleteDetail(@PathVariable String id) {
        if (detailService.softDelete(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
