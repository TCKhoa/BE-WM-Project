package org.wp.wpproject.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.wp.wpproject.entity.ImportReceiptDetail;

import java.util.List;
import java.util.Optional; // thêm dòng này

public interface ImportReceiptDetailRepository extends JpaRepository<ImportReceiptDetail, String> {

    List<ImportReceiptDetail> findByImportReceipt_IdAndDeletedAtIsNull(String receiptId);

    List<ImportReceiptDetail> findByDeletedAtIsNull();

    Optional<ImportReceiptDetail> findByIdAndDeletedAtIsNull(String id);
}
