package org.wp.wpproject.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.wp.wpproject.dto.ImportReceiptRequestDTO;
import org.wp.wpproject.dto.ImportReceiptResponseDTO;
import org.wp.wpproject.service.ImportReceiptService;

import java.util.List;

/**
 * Controller cho quản lý phiếu nhập kho
 */
@RestController
@RequestMapping("/api/import-receipts")
public class ImportReceiptController {

    @Autowired
    private ImportReceiptService importReceiptService;

    // ===================== LẤY DANH SÁCH =====================
    @GetMapping
    public ResponseEntity<List<ImportReceiptResponseDTO>> getAllActiveImportReceipts() {
        List<ImportReceiptResponseDTO> dtoList = importReceiptService.getAllActiveImportReceipts();
        return ResponseEntity.ok(dtoList);
    }

    // ===================== LẤY CHI TIẾT =====================
    @GetMapping("/{id}")
    public ResponseEntity<ImportReceiptResponseDTO> getImportReceiptById(@PathVariable String id) {
        return importReceiptService.getImportReceiptById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ===================== TẠO MỚI =====================
    @PostMapping
    public ResponseEntity<ImportReceiptResponseDTO> createImportReceipt(
            @RequestBody ImportReceiptRequestDTO requestDTO) {
        ImportReceiptResponseDTO savedDTO = importReceiptService.createImportReceipt(requestDTO);
        return ResponseEntity.ok(savedDTO);
    }

    // ===================== CẬP NHẬT =====================
    @PutMapping("/{id}")
    public ResponseEntity<ImportReceiptResponseDTO> updateImportReceipt(
            @PathVariable String id,
            @RequestBody ImportReceiptRequestDTO requestDTO) {
        return importReceiptService.updateImportReceipt(id, requestDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ===================== XÓA MỀM =====================
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDeleteImportReceipt(@PathVariable String id) {
        boolean deleted = importReceiptService.softDeleteImportReceipt(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
