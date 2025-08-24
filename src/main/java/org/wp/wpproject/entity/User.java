package org.wp.wpproject.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Where;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
// KHÔNG dùng @ToString để tránh vòng lặp
@Entity
@Table(name = "users")
@Where(clause = "deleted_at IS NULL")
public class User {

    @Id
    @Column(length = 50)
    private String id = UUID.randomUUID().toString(); // ✅ tự sinh ID

    @Column(name = "staff_code", length = 20, unique = true)
    private String staffCode;

    @Column(nullable = false, length = 100)
    private String username;

    @Column(nullable = false, length = 150, unique = true)
    private String email;

    @Column(length = 50)
    private String phone;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, length = 50)
    private String role;

    private LocalDate birthday;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

}
