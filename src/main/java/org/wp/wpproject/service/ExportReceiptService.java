package org.wp.wpproject.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.wp.wpproject.dto.ExportReceiptDetailRequestDTO;
import org.wp.wpproject.dto.ExportReceiptResponseDTO;
import org.wp.wpproject.entity.ExportReceipt;
import org.wp.wpproject.entity.ExportReceiptDetail;
import org.wp.wpproject.entity.Product;
import org.wp.wpproject.entity.User;
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
        // Sinh ID nếu chưa có
        if (exportReceipt.getId() == null || exportReceipt.getId().isBlank()) {
            exportReceipt.setId(UUID.randomUUID().toString().substring(0, 8));
        }

        // Set thời gian tạo
        if (exportReceipt.getCreatedAt() == null) {
            exportReceipt.setCreatedAt(LocalDateTime.now());
        }
        exportReceipt.setDeletedAt(null);

        // Gán thông tin người tạo
        if (exportReceipt.getCreatedBy() != null && exportReceipt.getCreatedBy().getId() != null) {
            userRepository.findById(exportReceipt.getCreatedBy().getId())
                    .ifPresent(exportReceipt::setCreatedBy);
        }

        // Xử lý chi tiết phiếu xuất
        List<ExportReceiptDetail> details = new ArrayList<>();

        if (detailDTOs != null && !detailDTOs.isEmpty()) {
            details = detailDTOs.stream().map(dto -> {
                // Kiểm tra sản phẩm tồn tại
                Product product = productRepository.findById(dto.getProductId())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm: " + dto.getProductId()));

                // Kiểm tra tồn kho
                if (product.getStock() < dto.getQuantity()) {
                    throw new RuntimeException("Sản phẩm " + product.getName() +
                            " chỉ còn số lượng " + product.getStock() + " trong kho!");
                }

                // Cập nhật tồn kho
                product.setStock(product.getStock() - dto.getQuantity());
                productRepository.save(product);

                // Tạo chi tiết phiếu
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

        // Lưu phiếu xuất
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

    // ===================== XÓA MỀM CÓ PHÂN QUYỀN + ROLLBACK TỒN KHO =====================
    public boolean softDelete(String id, User currentUser) {
        Optional<ExportReceipt> opt = exportReceiptRepository.findByIdAndDeletedAtIsNull(id);
        if (opt.isEmpty()) return false;

        ExportReceipt exportReceipt = opt.get();
        LocalDateTime now = LocalDateTime.now();

        String role = currentUser.getRole(); // staff, manager, admin
        LocalDateTime createdAt = exportReceipt.getCreatedAt();

        // Phân quyền xóa
        boolean allowed = switch (role.toLowerCase()) {
            case "admin" -> true; // Admin có thể xóa bất kỳ lúc nào
            case "manager" -> createdAt.plusWeeks(1).isAfter(now); // Manager có thể xóa trong vòng 1 tuần
            case "staff" -> createdAt.plusHours(24).isAfter(now); // Staff có thể xóa trong vòng 24h
            default -> false;
        };

        if (!allowed) {
            throw new RuntimeException("Bạn không có quyền xóa phiếu này.");
        }

        // ======= ROLLBACK TỒN KHO =======
        for (ExportReceiptDetail detail : exportReceipt.getDetails()) {
            Product product = productRepository.findById(detail.getProduct().getId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm để rollback: " + detail.getProduct().getId()));

            // Cộng lại số lượng đã xuất
            product.setStock(product.getStock() + detail.getQuantity());
            productRepository.save(product);
        }

        // Đánh dấu phiếu đã xóa (xóa mềm)
        exportReceipt.setDeletedAt(now);
        exportReceiptRepository.save(exportReceipt);

        return true;
    }

    // ===================== XÓA VĨNH VIỄN PHIẾU ĐÃ XÓA MỀM QUÁ 30 NGÀY =====================
    @Transactional
    public void permanentlyDeleteOldExportReceipts(int daysThreshold) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysThreshold);
        List<ExportReceipt> oldReceipts = exportReceiptRepository.findAllDeletedBefore(cutoffDate);

        if (!oldReceipts.isEmpty()) {
            exportReceiptRepository.deleteAll(oldReceipts);
            System.out.println("Đã xóa vĩnh viễn " + oldReceipts.size() + " phiếu xuất.");
        }
    }

}
