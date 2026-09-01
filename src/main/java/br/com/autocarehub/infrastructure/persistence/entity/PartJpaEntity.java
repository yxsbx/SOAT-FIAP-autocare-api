package br.com.autocarehub.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "parts")
public class PartJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(nullable = false, length = 60, unique = true)
    private String sku;

    @Column(nullable = false, length = 80)
    private String category;

    @Column(length = 80)
    private String subcategory;

    @Column(nullable = false, length = 80)
    private String brand;

    @Column(name = "cost_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal costPrice;

    @Column(name = "unit_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "stock_quantity", nullable = false)
    private int stockQuantity;

    @Column(name = "reserved_quantity", nullable = false)
    private int reservedQuantity;

    @Column(name = "minimum_stock", nullable = false)
    private int minimumStock;

    @Column(name = "reservation_days", nullable = false)
    private int reservationDays;

    @Column(name = "reservation_expires_at")
    private LocalDateTime reservationExpiresAt;

    @Column(nullable = false)
    private boolean active;
}
