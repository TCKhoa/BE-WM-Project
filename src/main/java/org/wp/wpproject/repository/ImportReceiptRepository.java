package org.wp.wpproject.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.wp.wpproject.entity.ImportReceipt;

import java.util.List;
import java.util.Optional;

public interface ImportReceiptRepository extends JpaRepository<ImportReceipt, String> {

    // Lấy tất cả import receipts chưa bị xóa
    List<ImportReceipt> findByDeletedAtIsNull();

    // Tìm import receipt theo id và chưa bị xóa

    Optional<ImportReceipt> findByIdAndDeletedAtIsNull(String id);
}
