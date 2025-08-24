package org.wp.wpproject.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "export_receipts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExportReceipt {

    @Id
    @Column(length = 50)
    private String id;

    @Column(name = "export_code", nullable = false, unique = true, length = 50)
    private String exportCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", referencedColumnName = "id", nullable = false)
    private User createdBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(columnDefinition = "text")
    private String note;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // Liên kết 1-N với chi tiết xuất
    @OneToMany(mappedBy = "exportReceipt", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ExportReceiptDetail> details;

    // Sinh ID và set createdAt trước khi persist
    @PrePersist
    public void prePersist() {
        if (this.id == null || this.id.isEmpty()) {
            this.id = UUID.randomUUID().toString();
        }
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}
