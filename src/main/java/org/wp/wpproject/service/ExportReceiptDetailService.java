package org.wp.wpproject.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.wp.wpproject.dto.ExportReceiptDetailRequestDTO;
import org.wp.wpproject.dto.ExportReceiptDetailResponseDTO;
import org.wp.wpproject.entity.ExportReceipt;
import org.wp.wpproject.entity.ExportReceiptDetail;
import org.wp.wpproject.entity.Product;
import org.wp.wpproject.exception.OutOfStockException;
import org.wp.wpproject.repository.ExportReceiptDetailRepository;
import org.wp.wpproject.repository.ExportReceiptRepository;
import org.wp.wpproject.repository.ProductRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ExportReceiptDetailService {

    private final ExportReceiptDetailRepository exportReceiptDetailRepository;
    private final ExportReceiptRepository exportReceiptRepository;
    private final ProductRepository productRepository;

    // ===================== LẤY DỮ LIỆU =====================
    public List<ExportReceiptDetailResponseDTO> getAll() {
        return exportReceiptDetailRepository.findByDeletedAtIsNull()
                .stream()
                .map(ExportReceiptDetailResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public Optional<ExportReceiptDetailResponseDTO> getById(String id) {
        return exportReceiptDetailRepository.findByIdAndDeletedAtIsNull(id)
                .map(ExportReceiptDetailResponseDTO::fromEntity);
    }

    public List<ExportReceiptDetailResponseDTO> getByReceiptId(String receiptId) {
        return exportReceiptDetailRepository.findByExportReceiptIdAndDeletedAtIsNull(receiptId)
                .stream()
                .map(ExportReceiptDetailResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // ===================== TẠO MỚI =====================
    public ExportReceiptDetailResponseDTO create(String exportReceiptId, ExportReceiptDetailRequestDTO dto) {
        // Lấy ExportReceipt
        ExportReceipt exportReceipt = exportReceiptRepository.findById(exportReceiptId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Export receipt not found"
                ));

        // Lấy Product
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Product not found"
                ));

        // ✅ Kiểm tra số lượng tồn kho
        if (product.getStock() < dto.getQuantity()) {
            throw new OutOfStockException(
                    "Sản phẩm " + product.getName() + " chỉ còn " + product.getStock() + " trong kho!"
            );
        }

        // ✅ Trừ số lượng sản phẩm
        product.setStock(product.getStock() - dto.getQuantity());
        productRepository.save(product);

        // Tạo entity chi tiết phiếu xuất
        ExportReceiptDetail detail = dto.toEntity(product);
        detail.setId(UUID.randomUUID().toString());
        detail.setExportReceipt(exportReceipt);
        detail.setDeletedAt(null);

        // Snapshot thông tin sản phẩm
        detail.setProductCode(product.getProductCode());
        detail.setProductName(product.getName());
        detail.setUnitName(product.getUnit() != null ? product.getUnit().getName() : null);

        ExportReceiptDetail saved = exportReceiptDetailRepository.save(detail);
        return ExportReceiptDetailResponseDTO.fromEntity(saved);
    }

    // ===================== XÓA MỀM =====================
    public boolean softDelete(String id) {
        return exportReceiptDetailRepository.findByIdAndDeletedAtIsNull(id)
                .map(detail -> {
                    detail.setDeletedAt(LocalDateTime.now());
                    exportReceiptDetailRepository.save(detail);
                    return true;
                })
                .orElse(false);
    }
}
