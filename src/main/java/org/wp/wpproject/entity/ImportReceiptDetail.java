package org.wp.wpproject.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "import_receipts_detail")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImportReceiptDetail {

    @Id
    @Column(length = 50)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "import_receipt_id", nullable = false)
    private ImportReceipt importReceipt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private int quantity;

    @Column( precision = 15, scale = 2)
    private BigDecimal price;

    @Column(name = "product_code", length = 100)
    private String productCode;

    @Column(name = "product_name", length = 255)
    private String productName;

    @Column(name = "price_snapshot", precision = 15, scale = 2)
    private BigDecimal priceSnapshot;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}

