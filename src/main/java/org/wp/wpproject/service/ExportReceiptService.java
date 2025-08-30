package org.wp.wpproject.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.wp.wpproject.dto.ExportReceiptDetailRequestDTO;
import org.wp.wpproject.dto.ExportReceiptResponseDTO;
import org.wp.wpproject.entity.ExportReceipt;
import org.wp.wpproject.entity.ExportReceiptDetail;
import org.wp.wpproject.entity.Product;
import org.wp.wpproject.entity.User; // <-- import User
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
        if (exportReceipt.getId() == null || exportReceipt.getId().isBlank()) {
            exportReceipt.setId(UUID.randomUUID().toString().substring(0, 8));
        }

        if (exportReceipt.getCreatedAt() == null) {
            exportReceipt.setCreatedAt(LocalDateTime.now());
        }
        exportReceipt.setDeletedAt(null);

        if (exportReceipt.getCreatedBy() != null && exportReceipt.getCreatedBy().getId() != null) {
            userRepository.findById(exportReceipt.getCreatedBy().getId())
                    .ifPresent(exportReceipt::setCreatedBy);
        }

        List<ExportReceiptDetail> details = new ArrayList<>();

        if (detailDTOs != null && !detailDTOs.isEmpty()) {
            details = detailDTOs.stream().map(dto -> {
                Product product = productRepository.findById(dto.getProductId())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm: " + dto.getProductId()));

                if (product.getStock() < dto.getQuantity()) {
                    throw new RuntimeException("Sản phẩm " + product.getName() + " chỉ còn số lượng " + product.getStock() + " trong kho!");
                }

                product.setStock(product.getStock() - dto.getQuantity());
                productRepository.save(product);

                ExportReceiptDetail detail = dto.toEntity(product);

                if (detail.getId() == null || detail.getId().isBlank()) {
                    detail.setId(UUID.randomUUID().toString().substring(0, 8));
                }

                detail.setExportReceipt(exportReceipt);
                detail.setDeletedAt(null);
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

    // ===================== XÓA MỀM CÓ PHÂN QUYỀN =====================
    public boolean softDelete(String id, User currentUser) {
        Optional<ExportReceipt> opt = exportReceiptRepository.findByIdAndDeletedAtIsNull(id);
        if (opt.isEmpty()) return false;

        ExportReceipt exportReceipt = opt.get();
        LocalDateTime now = LocalDateTime.now();

        String role = currentUser.getRole(); // staff, manager, admin
        LocalDateTime createdAt = exportReceipt.getCreatedAt();

        boolean allowed = switch (role.toLowerCase()) {
            case "admin" -> true;
            case "manager" -> createdAt.plusWeeks(1).isAfter(now);
            case "staff" -> createdAt.plusHours(24).isAfter(now);
            default -> false;
        };

        if (!allowed) {
            throw new RuntimeException("Bạn không có quyền xóa phiếu này.");
        }

        exportReceipt.setDeletedAt(now);
        exportReceiptRepository.save(exportReceipt);
        return true;
    }
}
