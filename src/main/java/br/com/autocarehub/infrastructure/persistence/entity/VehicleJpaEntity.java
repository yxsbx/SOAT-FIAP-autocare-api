package br.com.autocarehub.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "vehicles")
public class VehicleJpaEntity {

    @Id
    private UUID id;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(nullable = false, length = 8, unique = true)
    private String plate;

    @Column(nullable = false, length = 60)
    private String brand;

    @Column(nullable = false, length = 80)
    private String model;

    @Column(name = "manufacture_year", nullable = false)
    private int year;

    @Column(nullable = false)
    private int mileage;

    @Column(nullable = false)
    private boolean active;
}
