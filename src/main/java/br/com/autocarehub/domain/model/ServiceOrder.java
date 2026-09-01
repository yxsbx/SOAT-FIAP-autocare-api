package br.com.autocarehub.domain.model;

import br.com.autocarehub.domain.enums.ServiceOrderStatus;
import br.com.autocarehub.domain.exception.DomainException;
import br.com.autocarehub.domain.exception.InvalidServiceOrderStatusTransitionException;
import br.com.autocarehub.domain.valueobject.Money;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

public class ServiceOrder {

    private final UUID id;
    private final UUID customerId;
    private final UUID vehicleId;
    private final List<ServiceOrderService> services;
    private final List<ServiceOrderPart> parts;
    private final LocalDateTime createdAt;
    private final String diagnosticNotes;
    private ServiceOrderStatus status;
    private Money totalAmount;
    private @Nullable LocalDateTime budgetGeneratedAt;
    private @Nullable LocalDateTime approvedAt;
    private @Nullable LocalDateTime startedAt;
    private @Nullable LocalDateTime finishedAt;
    private @Nullable LocalDateTime deliveredAt;

    public ServiceOrder(UUID customerId, UUID vehicleId, String diagnosticNotes) {
        this(
                UUID.randomUUID(),
                customerId,
                vehicleId,
                ServiceOrderStatus.RECEBIDA,
                diagnosticNotes,
                List.of(),
                List.of(),
                Money.zero(),
                LocalDateTime.now(),
                null,
                null,
                null,
                null,
                null);
    }

    public ServiceOrder(
            UUID id,
            UUID customerId,
            UUID vehicleId,
            ServiceOrderStatus status,
            String diagnosticNotes,
            List<ServiceOrderService> services,
            List<ServiceOrderPart> parts,
            Money totalAmount,
            LocalDateTime createdAt,
            @Nullable LocalDateTime budgetGeneratedAt,
            @Nullable LocalDateTime approvedAt,
            @Nullable LocalDateTime startedAt,
            @Nullable LocalDateTime finishedAt,
            @Nullable LocalDateTime deliveredAt) {
        this.id = Objects.requireNonNull(id, "id is required");
        this.customerId = Objects.requireNonNull(customerId, "customerId is required");
        this.vehicleId = Objects.requireNonNull(vehicleId, "vehicleId is required");
        this.status = Objects.requireNonNull(status, "status is required");
        this.diagnosticNotes = requireText(diagnosticNotes, "Diagnostic notes are required");
        this.services = new ArrayList<>(Objects.requireNonNull(services, "services are required"));
        this.parts = new ArrayList<>(Objects.requireNonNull(parts, "parts are required"));
        this.totalAmount = Objects.requireNonNull(totalAmount, "totalAmount is required");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt is required");
        this.budgetGeneratedAt = budgetGeneratedAt;
        this.approvedAt = approvedAt;
        this.startedAt = startedAt;
        this.finishedAt = finishedAt;
        this.deliveredAt = deliveredAt;
    }

    private static String requireText(String value, String message) {
        if (value.isBlank()) {
            throw new DomainException(message);
        }
        return value.trim();
    }

    public void startDiagnosis() {
        requireStatus(ServiceOrderStatus.RECEBIDA, "Diagnosis can only start from received orders");
        this.status = ServiceOrderStatus.EM_DIAGNOSTICO;
    }

    public void addService(WorkshopService service, int quantity) {
        Objects.requireNonNull(service, "service is required");
        ensureCanChangeBudgetItems();
        this.services.add(new ServiceOrderService(service.id(), service.name(), quantity, service.basePrice()));
    }

    public void addPart(Part part, int quantity) {
        Objects.requireNonNull(part, "part is required");
        ensureCanChangeBudgetItems();
        if (!part.hasAvailableStock(quantity)) {
            throw new DomainException("Part stock is not available");
        }
        this.parts.add(new ServiceOrderPart(part.id(), part.name(), part.sku(), quantity, part.unitPrice()));
    }

    public Money generateBudget() {
        Budget budget = createBudget();
        this.totalAmount = budget.totalAmount();
        this.budgetGeneratedAt = LocalDateTime.now();
        this.status = ServiceOrderStatus.AGUARDANDO_APROVACAO;
        return totalAmount;
    }

    public void approveBudget() {
        requireStatus(ServiceOrderStatus.AGUARDANDO_APROVACAO, "Budget can only be approved while waiting approval");
        if (budgetGeneratedAt == null) {
            throw new DomainException("Budget must be generated before approval");
        }
        this.approvedAt = LocalDateTime.now();
    }

    public void rejectBudget() {
        requireStatus(ServiceOrderStatus.AGUARDANDO_APROVACAO, "Budget can only be rejected while waiting approval");
        if (budgetGeneratedAt == null) {
            throw new DomainException("Budget must be generated before rejection");
        }
        this.budgetGeneratedAt = null;
        this.approvedAt = null;
        this.status = ServiceOrderStatus.EM_DIAGNOSTICO;
    }

    public void startExecution() {
        requireStatus(ServiceOrderStatus.AGUARDANDO_APROVACAO, "Execution can only start after budget generation");
        if (approvedAt == null) {
            throw new DomainException("Execution cannot start without budget approval");
        }
        this.status = ServiceOrderStatus.EM_EXECUCAO;
        this.startedAt = LocalDateTime.now();
    }

    public void finish() {
        requireStatus(ServiceOrderStatus.EM_EXECUCAO, "Service order can only be finished while in progress");
        this.status = ServiceOrderStatus.FINALIZADA;
        this.finishedAt = LocalDateTime.now();
    }

    public void deliver() {
        requireStatus(ServiceOrderStatus.FINALIZADA, "Service order can only be delivered after finished");
        this.status = ServiceOrderStatus.ENTREGUE;
        this.deliveredAt = LocalDateTime.now();
    }

    public UUID id() {
        return id;
    }

    public UUID customerId() {
        return customerId;
    }

    public UUID vehicleId() {
        return vehicleId;
    }

    public ServiceOrderStatus status() {
        return status;
    }

    public String diagnosticNotes() {
        return diagnosticNotes;
    }

    public List<ServiceOrderService> services() {
        return Collections.unmodifiableList(services);
    }

    public List<ServiceOrderPart> parts() {
        return Collections.unmodifiableList(parts);
    }

    public Money totalAmount() {
        return totalAmount;
    }

    public Money servicesTotal() {
        return services.stream().map(ServiceOrderService::totalPrice).reduce(Money.zero(), Money::add);
    }

    public Money partsTotal() {
        return parts.stream().map(ServiceOrderPart::totalPrice).reduce(Money.zero(), Money::add);
    }

    public LocalDateTime createdAt() {
        return createdAt;
    }

    public @Nullable LocalDateTime budgetGeneratedAt() {
        return budgetGeneratedAt;
    }

    public @Nullable LocalDateTime approvedAt() {
        return approvedAt;
    }

    public @Nullable LocalDateTime startedAt() {
        return startedAt;
    }

    public @Nullable LocalDateTime finishedAt() {
        return finishedAt;
    }

    public @Nullable LocalDateTime deliveredAt() {
        return deliveredAt;
    }

    private Budget createBudget() {
        List<BudgetItem> budgetItems = new ArrayList<>();
        services.forEach(service -> budgetItems.add(
                new BudgetItem(service.serviceId(), service.name(), service.quantity(), service.unitPrice())));
        parts.forEach(
                part -> budgetItems.add(new BudgetItem(part.partId(), part.name(), part.quantity(), part.unitPrice())));
        return new Budget(budgetItems);
    }

    private void ensureCanChangeBudgetItems() {
        if (status == ServiceOrderStatus.AGUARDANDO_APROVACAO
                || status == ServiceOrderStatus.EM_EXECUCAO
                || status == ServiceOrderStatus.FINALIZADA
                || status == ServiceOrderStatus.ENTREGUE) {
            throw new InvalidServiceOrderStatusTransitionException(
                    "Service order items cannot be changed in current status");
        }
    }

    private void requireStatus(ServiceOrderStatus expected, String message) {
        if (status != expected) {
            throw new InvalidServiceOrderStatusTransitionException(message);
        }
    }

    public record ServiceOrderService(UUID serviceId, String name, int quantity, Money unitPrice, Money totalPrice) {

        public ServiceOrderService(UUID serviceId, String name, int quantity, Money unitPrice) {
            this(serviceId, name, quantity, unitPrice, unitPrice.multiply(quantity));
        }

        public ServiceOrderService {
            Objects.requireNonNull(serviceId, "serviceId is required");
            name = requireText(name, "Service name is required");
            if (quantity <= 0) {
                throw new DomainException("Quantity must be greater than zero");
            }
            Objects.requireNonNull(unitPrice, "unitPrice is required");
            Objects.requireNonNull(totalPrice, "totalPrice is required");
        }
    }

    public record ServiceOrderPart(
            UUID partId, String name, String sku, int quantity, Money unitPrice, Money totalPrice) {

        public ServiceOrderPart(UUID partId, String name, String sku, int quantity, Money unitPrice) {
            this(partId, name, sku, quantity, unitPrice, unitPrice.multiply(quantity));
        }

        public ServiceOrderPart {
            Objects.requireNonNull(partId, "partId is required");
            name = requireText(name, "Part name is required");
            sku = requireText(sku, "SKU is required");
            if (quantity <= 0) {
                throw new DomainException("Quantity must be greater than zero");
            }
            Objects.requireNonNull(unitPrice, "unitPrice is required");
            Objects.requireNonNull(totalPrice, "totalPrice is required");
        }
    }
}
