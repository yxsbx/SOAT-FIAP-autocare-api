package br.com.autocarehub.infrastructure.persistence.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "service_orders")
public class ServiceOrderJpaEntity {

    @OneToMany(mappedBy = "serviceOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private final Set<ServiceOrderServiceJpaEntity> services = new LinkedHashSet<>();

    @OneToMany(mappedBy = "serviceOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private final Set<ServiceOrderPartJpaEntity> parts = new LinkedHashSet<>();

    @Id
    private UUID id;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "vehicle_id", nullable = false)
    private UUID vehicleId;

    @Column(nullable = false, length = 30)
    private String status;

    @Column(name = "diagnostic_notes", nullable = false, length = 2000)
    private String diagnosticNotes;

    @Column(name = "total_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "budget_generated_at")
    private LocalDateTime budgetGeneratedAt;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    public void replaceServices(List<ServiceOrderServiceJpaEntity> newServices) {
        this.services.clear();
        newServices.forEach(this::addService);
    }

    public void replaceParts(List<ServiceOrderPartJpaEntity> newParts) {
        this.parts.clear();
        newParts.forEach(this::addPart);
    }

    public void addService(ServiceOrderServiceJpaEntity service) {
        service.setServiceOrder(this);
        this.services.add(service);
    }

    public void addPart(ServiceOrderPartJpaEntity part) {
        part.setServiceOrder(this);
        this.parts.add(part);
    }
}
