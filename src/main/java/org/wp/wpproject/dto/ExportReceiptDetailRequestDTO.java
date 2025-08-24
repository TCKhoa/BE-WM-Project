package org.wp.wpproject.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.wp.wpproject.entity.ExportReceiptDetail;
import org.wp.wpproject.entity.Product;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExportReceiptDetailRequestDTO {
    private String productId;
    private Integer quantity;
    private BigDecimal price; // Giá xuất có thể nhập, null → lấy từ product.price

    // Chuyển sang entity (chưa set snapshot info, service sẽ gán)
    public ExportReceiptDetail toEntity(Product product) {
        ExportReceiptDetail detail = new ExportReceiptDetail();
        detail.setProduct(product); // Bắt buộc phải set entity
        detail.setQuantity(quantity != null ? quantity : 0);
        detail.setPrice(price != null ? price : product.getImportPrice());
        return detail;
    }
}
