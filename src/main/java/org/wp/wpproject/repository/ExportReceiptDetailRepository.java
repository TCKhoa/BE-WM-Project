package org.wp.wpproject.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.wp.wpproject.entity.ExportReceiptDetail;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExportReceiptDetailRepository extends JpaRepository<ExportReceiptDetail, String> {

    // Lấy tất cả chưa bị xóa
    List<ExportReceiptDetail> findByDeletedAtIsNull();

    // Lấy 1 bản ghi theo id (chưa bị xóa)
    Optional<ExportReceiptDetail> findByIdAndDeletedAtIsNull(String id);

    // Lấy tất cả chi tiết phiếu xuất theo exportReceiptId (chưa bị xóa)
    List<ExportReceiptDetail> findByExportReceiptIdAndDeletedAtIsNull(String exportReceiptId);
}
