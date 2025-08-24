package org.wp.wpproject.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.wp.wpproject.entity.ExportReceipt;
import java.util.List;
import java.util.Optional;

public interface ExportReceiptRepository extends JpaRepository<ExportReceipt, String> {

    // Lấy tất cả export receipts chưa bị xóa
    List<ExportReceipt> findByDeletedAtIsNull();

    // Lấy export receipt theo id chưa bị xóa
    Optional<ExportReceipt> findByIdAndDeletedAtIsNull(String id);
}
