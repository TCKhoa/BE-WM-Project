package org.wp.wpproject.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExportReceiptRequestDTO {
    private String exportCode;
    private String createdById; // user id
    private String note;
    private List<ExportReceiptDetailRequestDTO> details;
}