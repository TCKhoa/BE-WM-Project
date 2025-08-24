package org.wp.wpproject.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "revenues")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Revenue {

    @Id
    @Column(length = 50)
    private String id;

    @Column(nullable = false)
    private LocalDate date;

    @Column(name = "total_import", precision = 15, scale = 2)
    private BigDecimal totalImport = BigDecimal.valueOf(0.00);

    @Column(name = "total_export", precision = 15, scale = 2)
    private BigDecimal totalExport = BigDecimal.valueOf(0.00);

    @Column(name = "total_profit", precision = 15, scale = 2)
    private BigDecimal totalProfit = BigDecimal.valueOf(0.00);
}
