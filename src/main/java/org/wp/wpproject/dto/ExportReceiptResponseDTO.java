package org.wp.wpproject.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.wp.wpproject.entity.ExportReceipt;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExportReceiptResponseDTO {
    private String id;
    private String exportCode;
    private String createdById;
    private String createdByUsername;
    private LocalDateTime createdAt;
    private String note;
    private List<ExportReceiptDetailResponseDTO> details;

    // Chuyển từ entity sang DTO
    public static ExportReceiptResponseDTO fromEntity(ExportReceipt entity) {
        return ExportReceiptResponseDTO.builder()
                .id(entity.getId())
                .exportCode(entity.getExportCode())
                .createdAt(entity.getCreatedAt())
                .note(entity.getNote())
                .createdByUsername(entity.getCreatedBy() != null ? entity.getCreatedBy().getUsername() : null)
                .details(entity.getDetails() != null
                        ? entity.getDetails().stream()
                        .map(ExportReceiptDetailResponseDTO::fromEntity)
                        .toList()
                        : null)
                .build();
    }
}