package org.wp.wpproject.dto;

import lombok.Builder;
import lombok.Data;
import org.wp.wpproject.entity.ImportReceipt;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ImportReceiptResponseDTO {

    private String id;
    private String importCode;
    private String createdBy;   // username người tạo
    private String createdById;
    private LocalDateTime createdAt;
    private String note;
    private BigDecimal totalAmount; // ✅ dùng BigDecimal thay vì Double
    private List<ImportReceiptDetailResponseDTO> details;

    public static ImportReceiptResponseDTO fromEntity(ImportReceipt entity) {
        BigDecimal total = BigDecimal.ZERO;

        if (entity.getDetails() != null) {
            total = entity.getDetails().stream()
                    .map(d -> d.getPrice().multiply(BigDecimal.valueOf(d.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        return ImportReceiptResponseDTO.builder()
                .id(entity.getId())
                .importCode(entity.getImportCode())
                .createdBy(entity.getCreatedBy() != null ? entity.getCreatedBy().getUsername() : null)
                .createdAt(entity.getCreatedAt())
                .note(entity.getNote())
                .totalAmount(total) // ✅ tổng tiền bằng BigDecimal
                .details(entity.getDetails() != null ?
                        entity.getDetails().stream()
                                .map(ImportReceiptDetailResponseDTO::fromEntity)
                                .toList()
                        : List.of())
                .build();
    }
}
