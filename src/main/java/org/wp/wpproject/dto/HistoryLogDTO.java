package org.wp.wpproject.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HistoryLogDTO {
    private String id;
    private String action;
    private String userId;
    private String username;
    private String status;
    private LocalDateTime performedAt;
    private LocalDateTime deletedAt;
    private Boolean isRead;  // thêm vào HistoryLogDTO

}
