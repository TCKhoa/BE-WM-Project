package org.wp.wpproject.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.wp.wpproject.dto.ImportReceiptDetailRequestDTO;
import org.wp.wpproject.dto.ImportReceiptDetailResponseDTO;
import org.wp.wpproject.entity.ImportReceipt;
import org.wp.wpproject.entity.ImportReceiptDetail;
import org.wp.wpproject.entity.Product;
import org.wp.wpproject.repository.ImportReceiptDetailRepository;
import org.wp.wpproject.repository.ImportReceiptRepository;
import org.wp.wpproject.repository.ProductRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ImportReceiptDetailService {

    private final ImportReceiptDetailRepository importReceiptDetailRepository;
    private final ImportReceiptRepository importReceiptRepository;
    private final ProductRepository productRepository;

    // ===================== LẤY DỮ LIỆU =====================
    public List<ImportReceiptDetailResponseDTO> getAll() {
        return importReceiptDetailRepository.findByDeletedAtIsNull()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public Optional<ImportReceiptDetailResponseDTO> getById(String id) {
        return importReceiptDetailRepository.findByIdAndDeletedAtIsNull(id)
                .map(this::toDTO);
    }

    public List<ImportReceiptDetailResponseDTO> getByReceiptId(String receiptId) {
        return importReceiptDetailRepository.findByImportReceipt_IdAndDeletedAtIsNull(receiptId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ===================== TẠO MỚI (với snapshot product info) =====================
    public ImportReceiptDetailResponseDTO create(String importReceiptId, ImportReceiptDetailRequestDTO dto) {
        // Lấy ImportReceipt
        ImportReceipt importReceipt = importReceiptRepository.findById(importReceiptId)
                .orElseThrow(() -> new RuntimeException("Import receipt not found"));

        // Lấy Product
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Tạo entity mới từ DTO
        ImportReceiptDetail detail = dto.toEntity(product);
        detail.setId(UUID.randomUUID().toString());
        detail.setImportReceipt(importReceipt);
        detail.setDeletedAt(null);

        // ----------------- FIX: snapshot product info -----------------
        detail.setProductCode(product.getProductCode());
        detail.setProductName(product.getName());
        detail.setPrice(product.getImportPrice());          // giá hiện tại
        detail.setPriceSnapshot(product.getImportPrice());  // giá snapshot

        ImportReceiptDetail saved = importReceiptDetailRepository.save(detail);
        return toDTO(saved);
    }

    // ===================== XÓA MỀM =====================
    public boolean softDelete(String id) {
        Optional<ImportReceiptDetail> optionalDetail = importReceiptDetailRepository.findByIdAndDeletedAtIsNull(id);
        if (optionalDetail.isEmpty()) {
            return false;
        }
        ImportReceiptDetail detail = optionalDetail.get();
        detail.setDeletedAt(LocalDateTime.now());
        importReceiptDetailRepository.save(detail);
        return true;
    }

    // ===================== HỖ TRỢ CONVERT =====================
    private ImportReceiptDetailResponseDTO toDTO(ImportReceiptDetail entity) {
        return ImportReceiptDetailResponseDTO.builder()
                .id(entity.getId())
                .importReceiptId(entity.getImportReceipt().getId())
                .productId(entity.getProduct().getId())
                .productCode(entity.getProductCode())
                .productName(entity.getProductName())
                .quantity(entity.getQuantity())
                .price(entity.getPrice())
                .priceSnapshot(entity.getPriceSnapshot())
                .build();
    }
}
