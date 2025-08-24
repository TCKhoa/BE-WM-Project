package org.wp.wpproject.dto;

import lombok.Data;
import java.util.List;

/**
 * DTO dùng khi tạo hoặc cập nhật phiếu nhập kho (ImportReceipt).
 * Client sẽ gửi dữ liệu dạng JSON theo cấu trúc này.
 */
@Data
public class ImportReceiptRequestDTO {

    private String importCode;       // Mã phiếu nhập
    private String createdById;
    private String createdBy;// ID của User tạo phiếu (UUID dưới dạng String)
    private String note;             // Ghi chú
    private List<ImportReceiptDetailRequestDTO> details; // Danh sách chi tiết phiếu nhập
}
