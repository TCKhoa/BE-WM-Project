package org.wp.wpproject.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "export_receipts_detail")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExportReceiptDetail {

    @Id
    @Column(length = 50)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "export_receipt_id", nullable = false)
    private ExportReceipt exportReceipt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal price;

    // snapshot thông tin sản phẩm
    private String productCode;
    private String productName;
    private String unitName;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // Gán UUID khi tạo mới nếu chưa có ID
    @PrePersist
    public void prePersist() {
        if (this.id == null || this.id.isEmpty()) {
            this.id = UUID.randomUUID().toString();
        }
    }
}
