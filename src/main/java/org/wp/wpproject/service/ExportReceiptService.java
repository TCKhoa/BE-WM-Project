package org.wp.wpproject.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.wp.wpproject.dto.ExportReceiptDetailRequestDTO;
import org.wp.wpproject.dto.ExportReceiptResponseDTO;
import org.wp.wpproject.entity.ExportReceipt;
import org.wp.wpproject.entity.ExportReceiptDetail;
import org.wp.wpproject.entity.Product;
import org.wp.wpproject.repository.ExportReceiptRepository;
import org.wp.wpproject.repository.ProductRepository;
import org.wp.wpproject.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ExportReceiptService {

    private final ExportReceiptRepository exportReceiptRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    // ===================== LẤY TẤT CẢ (chưa xóa) =====================
    public List<ExportReceiptResponseDTO> getAllDTO() {
        return exportReceiptRepository.findByDeletedAtIsNull()
                .stream()
                .map(ExportReceiptResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // ===================== LẤY THEO ID (chưa xóa) =====================
    public Optional<ExportReceiptResponseDTO> getByIdDTO(String id) {
        return exportReceiptRepository.findByIdAndDeletedAtIsNull(id)
                .map(ExportReceiptResponseDTO::fromEntity);
    }

    // ===================== TẠO MỚI =====================
    public ExportReceiptResponseDTO create(ExportReceipt exportReceipt, List<ExportReceiptDetailRequestDTO> detailDTOs) {
        // Sinh ID ngắn gọn nếu chưa có
        if (exportReceipt.getId() == null || exportReceipt.getId().isBlank()) {
            exportReceipt.setId(UUID.randomUUID().toString().substring(0, 8));
        }
        // Gán ngày tạo
        if (exportReceipt.getCreatedAt() == null) {
            exportReceipt.setCreatedAt(LocalDateTime.now());
        }
        exportReceipt.setDeletedAt(null);

        // Gán user tạo phiếu nếu có
        if (exportReceipt.getCreatedBy() != null && exportReceipt.getCreatedBy().getId() != null) {
            userRepository.findById(exportReceipt.getCreatedBy().getId())
                    .ifPresent(exportReceipt::setCreatedBy);
        }

        // Danh sách chi tiết phiếu
        List<ExportReceiptDetail> details = new ArrayList<>();

        if (detailDTOs != null && !detailDTOs.isEmpty()) {
            details = detailDTOs.stream().map(dto -> {
                Product product = productRepository.findById(dto.getProductId())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm: " + dto.getProductId()));

                // Kiểm tra tồn kho
                if (product.getStock() < dto.getQuantity()) {
                    throw new RuntimeException("Sản phẩm " + product.getName() + " chỉ còn số lượng " + product.getStock()+ " trong kho!");
                }

                // Giảm số lượng tồn kho
                product.setStock(product.getStock() - dto.getQuantity());
                productRepository.save(product);

                // Tạo chi tiết phiếu xuất
                ExportReceiptDetail detail = dto.toEntity(product);

                if (detail.getId() == null || detail.getId().isBlank()) {
                    detail.setId(UUID.randomUUID().toString().substring(0, 8));
                }

                detail.setExportReceipt(exportReceipt);
                detail.setDeletedAt(null);

                // Snapshot thông tin sản phẩm (tránh thay đổi sau này)
                detail.setProductCode(product.getProductCode());
                detail.setProductName(product.getName());
                detail.setUnitName(product.getUnit() != null ? product.getUnit().getName() : null);

                return detail;
            }).collect(Collectors.toList());
        }

        exportReceipt.setDetails(details);

        ExportReceipt saved = exportReceiptRepository.save(exportReceipt);
        return ExportReceiptResponseDTO.fromEntity(saved);
    }

    // ===================== CẬP NHẬT =====================
    public Optional<ExportReceiptResponseDTO> update(String id, ExportReceipt exportReceiptDetails) {
        Optional<ExportReceipt> opt = exportReceiptRepository.findByIdAndDeletedAtIsNull(id);
        if (opt.isEmpty()) return Optional.empty();

        ExportReceipt exportReceipt = opt.get();

        if (exportReceiptDetails.getExportCode() != null) {
            exportReceipt.setExportCode(exportReceiptDetails.getExportCode());
        }
        if (exportReceiptDetails.getNote() != null) {
            exportReceipt.setNote(exportReceiptDetails.getNote());
        }

        if (exportReceiptDetails.getCreatedBy() != null && exportReceiptDetails.getCreatedBy().getId() != null) {
            userRepository.findById(exportReceiptDetails.getCreatedBy().getId())
                    .ifPresent(exportReceipt::setCreatedBy);
        }

        ExportReceipt updated = exportReceiptRepository.save(exportReceipt);
        return Optional.of(ExportReceiptResponseDTO.fromEntity(updated));
    }

    // ===================== XÓA MỀM =====================
    public boolean softDelete(String id) {
        Optional<ExportReceipt> opt = exportReceiptRepository.findByIdAndDeletedAtIsNull(id);
        if (opt.isEmpty()) return false;

        ExportReceipt exportReceipt = opt.get();
        exportReceipt.setDeletedAt(LocalDateTime.now());
        exportReceiptRepository.save(exportReceipt);
        return true;
    }
}
