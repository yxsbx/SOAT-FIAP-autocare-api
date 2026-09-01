package br.com.autocarehub.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "customers")
public class CustomerJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(name = "document_type", nullable = false, length = 10)
    private String documentType;

    @Column(name = "document_value", nullable = false, length = 14, unique = true)
    private String documentValue;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(nullable = false, length = 120)
    private String email;

    @Column(name = "address_street", length = 120)
    private String addressStreet;

    @Column(name = "address_number", length = 20)
    private String addressNumber;

    @Column(name = "address_complement", length = 80)
    private String addressComplement;

    @Column(name = "address_neighborhood", length = 80)
    private String addressNeighborhood;

    @Column(name = "address_city", length = 80)
    private String addressCity;

    @Column(name = "address_state", length = 2)
    private String addressState;

    @Column(name = "address_zip_code", length = 8)
    private String addressZipCode;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
