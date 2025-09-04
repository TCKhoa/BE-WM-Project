package org.wp.wpproject.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.wp.wpproject.dto.ImportReceiptRequestDTO;
import org.wp.wpproject.dto.ImportReceiptResponseDTO;
import org.wp.wpproject.entity.ImportReceipt;
import org.wp.wpproject.entity.ImportReceiptDetail;
import org.wp.wpproject.entity.Product;
import org.wp.wpproject.entity.User;
import org.wp.wpproject.repository.ImportReceiptRepository;
import org.wp.wpproject.repository.ProductRepository;
import org.wp.wpproject.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ImportReceiptService {

    @Autowired
    private ImportReceiptRepository importReceiptRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    // ===================== LẤY TẤT CẢ (chưa xóa) =====================
    public List<ImportReceiptResponseDTO> getAllActiveImportReceipts() {
        return importReceiptRepository.findByDeletedAtIsNull()
                .stream()
                .map(ImportReceiptResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // ===================== LẤY THEO ID (chưa xóa) =====================
    public Optional<ImportReceiptResponseDTO> getImportReceiptById(String id) {
        return importReceiptRepository.findByIdAndDeletedAtIsNull(id)
                .map(ImportReceiptResponseDTO::fromEntity);
    }

    // ===================== TẠO MỚI =====================
    @Transactional
    public ImportReceiptResponseDTO createImportReceipt(ImportReceiptRequestDTO request) {
        ImportReceipt importReceipt = new ImportReceipt();

        // 🔹 Tự sinh ID cho ImportReceipt
        importReceipt.setId(UUID.randomUUID().toString());
        importReceipt.setImportCode(request.getImportCode());
        importReceipt.setNote(request.getNote());
        importReceipt.setCreatedAt(LocalDateTime.now());
        importReceipt.setDeletedAt(null);

        // 🔹 Gán user tạo phiếu
        if (request.getCreatedById() != null) {
            userRepository.findById(request.getCreatedById()).ifPresent(importReceipt::setCreatedBy);
        }

        // 🔹 Gán chi tiết phiếu nhập
        List<ImportReceiptDetail> details = request.getDetails().stream().map(d -> {
            ImportReceiptDetail detail = new ImportReceiptDetail();
            detail.setId(UUID.randomUUID().toString()); // ID chi tiết

            productRepository.findById(d.getProductId()).ifPresent(product -> {
                detail.setProduct(product);
                detail.setPrice(product.getImportPrice());           // giá nhập hiện tại
                detail.setPriceSnapshot(product.getImportPrice());  // snapshot giá
                detail.setProductCode(product.getProductCode());    // snapshot mã sản phẩm
                detail.setProductName(product.getName());           // snapshot tên sản phẩm
            });

            detail.setQuantity(d.getQuantity());
            detail.setImportReceipt(importReceipt);
            return detail;
        }).collect(Collectors.toList());
        importReceipt.setDetails(details);

        ImportReceipt saved = importReceiptRepository.save(importReceipt);

        // 🔹 Cập nhật tồn kho sản phẩm (CỘNG thêm số lượng nhập)
        for (ImportReceiptDetail detail : saved.getDetails()) {
            Product product = detail.getProduct();
            if (product != null) {
                int newQuantity = product.getStock() + detail.getQuantity();
                product.setStock(newQuantity);
                productRepository.save(product);
            }
        }

        return ImportReceiptResponseDTO.fromEntity(saved);
    }

    // ===================== CẬP NHẬT =====================
    @Transactional
    public Optional<ImportReceiptResponseDTO> updateImportReceipt(String id, ImportReceiptRequestDTO request) {
        Optional<ImportReceipt> importReceiptOpt = importReceiptRepository.findByIdAndDeletedAtIsNull(id);
        if (importReceiptOpt.isEmpty()) {
            return Optional.empty();
        }

        ImportReceipt oldReceipt = importReceiptOpt.get();

        // 🔹 Rollback tồn kho cũ (TRỪ đi số lượng đã nhập trước đó)
        if (oldReceipt.getDetails() != null) {
            for (ImportReceiptDetail oldDetail : oldReceipt.getDetails()) {
                Product product = oldDetail.getProduct();
                if (product != null) {
                    int rollbackQuantity = product.getStock() - oldDetail.getQuantity();
                    product.setStock(Math.max(rollbackQuantity, 0));
                    productRepository.save(product);
                }
            }
        }

        // 🔹 Cập nhật thông tin mới
        oldReceipt.setImportCode(request.getImportCode());
        oldReceipt.setNote(request.getNote());
        oldReceipt.setUpdatedAt(LocalDateTime.now());

        if (request.getCreatedById() != null) {
            userRepository.findById(request.getCreatedById()).ifPresent(oldReceipt::setCreatedBy);
        }

        // 🔹 Cập nhật chi tiết (xóa cũ, thêm mới)
        List<ImportReceiptDetail> newDetails = request.getDetails().stream().map(d -> {
            ImportReceiptDetail detail = new ImportReceiptDetail();
            detail.setId(UUID.randomUUID().toString());

            productRepository.findById(d.getProductId()).ifPresent(product -> {
                detail.setProduct(product);
                detail.setPrice(product.getImportPrice());
                detail.setPriceSnapshot(product.getImportPrice());
                detail.setProductCode(product.getProductCode());
                detail.setProductName(product.getName());
            });

            detail.setQuantity(d.getQuantity());
            detail.setImportReceipt(oldReceipt);
            return detail;
        }).collect(Collectors.toList());
        oldReceipt.setDetails(newDetails);

        ImportReceipt updated = importReceiptRepository.save(oldReceipt);

        // 🔹 Cập nhật tồn kho theo chi tiết mới (CỘNG lại số lượng nhập mới)
        for (ImportReceiptDetail detail : updated.getDetails()) {
            Product product = detail.getProduct();
            if (product != null) {
                int newQuantity = product.getStock() + detail.getQuantity();
                product.setStock(newQuantity);
                productRepository.save(product);
            }
        }

        return Optional.of(ImportReceiptResponseDTO.fromEntity(updated));
    }

    // ===================== XÓA MỀM =====================
    @Transactional
    public boolean softDeleteImportReceipt(String id, User currentUser) {
        Optional<ImportReceipt> importReceiptOpt = importReceiptRepository.findByIdAndDeletedAtIsNull(id);
        if (importReceiptOpt.isEmpty()) {
            return false;
        }
        ImportReceipt importReceipt = importReceiptOpt.get();

        // Kiểm tra quyền xóa theo role
        String role = currentUser.getRole(); // giả sử User có field role
        LocalDateTime createdAt = importReceipt.getCreatedAt();
        LocalDateTime now = LocalDateTime.now();

        boolean canDelete = false;

        if ("admin".equals(role)) {
            canDelete = true; // admin xóa bất cứ lúc nào
        } else if ("manager".equals(role)) {
            canDelete = createdAt.plusDays(7).isAfter(now); // manager trong 7 ngày
        } else if ("staff".equals(role)) {
            canDelete = createdAt.plusDays(1).isAfter(now); // staff trong 1 ngày
        }

        if (!canDelete) {
            return false; // không đủ quyền hoặc hết hạn xóa
        }

        // 🔹 Rollback tồn kho khi xóa (TRỪ đi số lượng đã nhập từ phiếu này)
        if (importReceipt.getDetails() != null) {
            for (ImportReceiptDetail detail : importReceipt.getDetails()) {
                Product product = detail.getProduct();
                if (product != null) {
                    int rollbackQuantity = product.getStock() - detail.getQuantity();
                    product.setStock(Math.max(rollbackQuantity, 0)); // tránh âm
                    productRepository.save(product);
                }
            }
        }

        // 🔹 Đánh dấu đã xóa mềm
        importReceipt.setDeletedAt(LocalDateTime.now());
        importReceiptRepository.save(importReceipt);

        return true;
    }

}
