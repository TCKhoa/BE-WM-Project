package org.wp.wpproject.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.wp.wpproject.entity.ExportReceipt;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ExportReceiptRepository extends JpaRepository<ExportReceipt, String> {

    /**
     * Lấy tất cả phiếu xuất chưa bị xóa mềm
     *
     * @return danh sách ExportReceipt chưa bị xóa
     */
    List<ExportReceipt> findByDeletedAtIsNull();

    /**
     * Lấy phiếu xuất theo id chưa bị xóa mềm
     *
     * @param id mã phiếu xuất
     * @return Optional chứa ExportReceipt nếu tìm thấy
     */
    Optional<ExportReceipt> findByIdAndDeletedAtIsNull(String id);

    /**
     * Lấy tất cả phiếu xuất đã bị xóa mềm trước thời điểm nhất định
     * => Dùng cho Scheduler để xóa vĩnh viễn
     *
     * @param thresholdDate thời điểm cắt
     * @return danh sách ExportReceipt đã bị xóa mềm và cũ hơn thresholdDate
     */
    @Query("SELECT e FROM ExportReceipt e WHERE e.deletedAt IS NOT NULL AND e.deletedAt < :thresholdDate")
    List<ExportReceipt> findAllDeletedBefore(@Param("thresholdDate") LocalDateTime thresholdDate);

}
