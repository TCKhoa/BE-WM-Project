package org.wp.wpproject.dto;

import lombok.Builder;
import lombok.Data;
import org.wp.wpproject.entity.ImportReceiptDetail;

import java.math.BigDecimal;

/**
 * DTO cho chi tiết phiếu nhập (dùng để trả về frontend)
 */
@Data
@Builder
public class ImportReceiptDetailResponseDTO {

    private String id;
    private String importReceiptId;
    private String productId;
    private String productCode;
    private String productName;
    private String categoryName; // tên danh mục
    private String brandName;    // tên thương hiệu
    private String unitName;     // tên đơn vị
    private int quantity;
    private BigDecimal price;
    private BigDecimal priceSnapshot;

    /**
     * Chuyển từ entity -> DTO
     */
    public static ImportReceiptDetailResponseDTO fromEntity(ImportReceiptDetail entity) {
        return ImportReceiptDetailResponseDTO.builder()
                .id(entity.getId())
                .importReceiptId(entity.getImportReceipt() != null ? entity.getImportReceipt().getId() : null)
                .productId(entity.getProduct() != null ? entity.getProduct().getId() : null)
                .productCode(entity.getProductCode())
                .productName(entity.getProductName())
                .categoryName(entity.getProduct() != null && entity.getProduct().getCategory() != null
                        ? entity.getProduct().getCategory().getName() : "")
                .brandName(entity.getProduct() != null && entity.getProduct().getBrand() != null
                        ? entity.getProduct().getBrand().getName() : "")
                .unitName(entity.getProduct() != null && entity.getProduct().getUnit() != null
                        ? entity.getProduct().getUnit().getName() : "")
                .quantity(entity.getQuantity())
                .price(entity.getPrice())
                .priceSnapshot(entity.getPriceSnapshot())
                .build();
    }
}
