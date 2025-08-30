package org.wp.wpproject.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.wp.wpproject.dto.ImportReceiptRequestDTO;
import org.wp.wpproject.dto.ImportReceiptResponseDTO;
import org.wp.wpproject.entity.User;
import org.wp.wpproject.service.HistoryLogService;
import org.wp.wpproject.service.ImportReceiptService;
import org.wp.wpproject.service.UserService;

import java.util.List;

/**
 * Controller quản lý phiếu nhập kho
 * Mỗi hành động tạo, cập nhật, xóa đều ghi vào HistoryLog
 */
@RestController
@RequestMapping("/api/import-receipts")
@AllArgsConstructor
public class ImportReceiptController {

    private final ImportReceiptService importReceiptService;
    private final HistoryLogService historyLogService;
    private final UserService userService;

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

        // Ghi log hành động với user hiện tại
        User currentUser = getCurrentUser();
        if (currentUser != null) {
            historyLogService.logAction("Tạo phiếu nhập " + savedDTO.getImportCode(), currentUser);
        }

        return ResponseEntity.ok(savedDTO);
    }

    // ===================== CẬP NHẬT =====================
    @PutMapping("/{id}")
    public ResponseEntity<ImportReceiptResponseDTO> updateImportReceipt(
            @PathVariable String id,
            @RequestBody ImportReceiptRequestDTO requestDTO) {

        return importReceiptService.updateImportReceipt(id, requestDTO)
                .map(updatedDTO -> {
                    User currentUser = getCurrentUser();
                    if (currentUser != null) {
                        historyLogService.logAction("Cập nhật phiếu nhập " + updatedDTO.getImportCode(), currentUser);
                    }
                    return ResponseEntity.ok(updatedDTO);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // ===================== XÓA MỀM =====================
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDeleteImportReceipt(@PathVariable String id) {
        User currentUser = getCurrentUser();

        if (currentUser == null) {
            return ResponseEntity.status(403).build(); // chưa login hoặc không có quyền
        }

        // Lấy phiếu trước khi xóa để có importCode
        var importReceiptOpt = importReceiptService.getImportReceiptById(id);

        boolean deleted = importReceiptService.softDeleteImportReceipt(id, currentUser);

        if (deleted && importReceiptOpt.isPresent()) {
            String importCode = importReceiptOpt.get().getImportCode();
            historyLogService.logAction("Xóa phiếu nhập " + importCode, currentUser);
        }

        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.status(403).build();
    }



    // ===================== HỖ TRỢ LẤY NGƯỜI DÙNG =====================
    private User getCurrentUser() {
        try {
            return userService.getCurrentUser();
        } catch (Exception e) {
            // Nếu chưa login, trả về null
            return null;
        }
    }
}
