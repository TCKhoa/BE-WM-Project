package org.wp.wpproject.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "history_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistoryLog {

    @Id
    @Column(length = 50)
    private String id;

    @Column(nullable = false, length = 255)
    private String action;

    // EAGER để frontend lấy username trực tiếp
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "performed_at", nullable = false)
    private LocalDateTime performedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "status", length = 20)
    private String status;   // SUCCESS / FAILED
    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;  // mặc định là chưa đọc

}
