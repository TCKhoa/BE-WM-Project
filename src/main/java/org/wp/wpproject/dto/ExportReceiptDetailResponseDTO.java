package org.wp.wpproject.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.wp.wpproject.entity.ExportReceiptDetail;

import java.math.BigDecimal;

/**
 * DTO trả về cho chi tiết phiếu xuất
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExportReceiptDetailResponseDTO {

    private String id;
    private String exportReceiptId;
    private String productId;
    private String productCode;
    private String productName;
    private String unitName;
    private Integer quantity;
    private BigDecimal price;

    /**
     * Chuyển từ entity sang DTO
     */
    public static ExportReceiptDetailResponseDTO fromEntity(ExportReceiptDetail entity) {
        if (entity == null) {
            return null;
        }

        return ExportReceiptDetailResponseDTO.builder()
                .id(entity.getId())
                .exportReceiptId(
                        entity.getExportReceipt() != null
                                ? entity.getExportReceipt().getId()
                                : null
                )
                .productId(
                        entity.getProduct() != null
                                ? entity.getProduct().getId()
                                : null
                )
                .productCode(entity.getProductCode() != null ? entity.getProductCode() : "")
                .productName(entity.getProductName() != null ? entity.getProductName() : "")
                .unitName(entity.getUnitName() != null ? entity.getUnitName() : "")
                .quantity(entity.getQuantity()) // int nên luôn có giá trị
                .price(entity.getPrice() != null ? entity.getPrice() : BigDecimal.ZERO)
                .build();
    }
}
