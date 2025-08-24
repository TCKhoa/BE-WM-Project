package org.wp.wpproject.dto;

import lombok.Data;
import org.wp.wpproject.entity.ImportReceiptDetail;
import org.wp.wpproject.entity.Product;

import java.math.BigDecimal;

/**
 * DTO cho chi tiết phiếu nhập (frontend gửi lên).
 */
@Data
public class ImportReceiptDetailRequestDTO {

    private String productId;  // ID sản phẩm
    private int quantity;      // Số lượng nhập
    private BigDecimal price;  // Giá nhập của sản phẩm

    /**
     * Chuyển DTO -> Entity (ImportReceipt sẽ set trong Service).
     */
    public ImportReceiptDetail toEntity(Product product) {
        ImportReceiptDetail detail = new ImportReceiptDetail();
        detail.setProduct(product);
        detail.setProductCode(product.getProductCode());
        detail.setProductName(product.getName());
        detail.setQuantity(quantity);
        detail.setPrice(price);
        detail.setPriceSnapshot(price);
        return detail;
    }
}
