package br.com.autocarehub.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.autocarehub.domain.enums.ServiceOrderStatus;
import br.com.autocarehub.domain.exception.DomainException;
import br.com.autocarehub.domain.exception.InvalidServiceOrderStatusTransitionException;
import br.com.autocarehub.domain.valueobject.Money;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ServiceOrderTest {

    private static ServiceOrder serviceOrderWithItems() {
        ServiceOrder serviceOrder = serviceOrder();
        serviceOrder.addService(
                new WorkshopService("Oil change", "Oil and filter replacement", Money.of("100.00"), 60), 2);
        serviceOrder.addPart(part("OIL-001", 10, 2), 4);
        return serviceOrder;
    }

    private static ServiceOrder serviceOrder() {
        return new ServiceOrder(java.util.UUID.randomUUID(), java.util.UUID.randomUUID(), "Initial diagnostic notes");
    }

    private static Part part(String sku, int stockQuantity, int minimumStock) {
        return Part.create(
                new Part.CatalogData("Oil filter", "Oil filter", sku, "Filters", null, "Bosch"),
                Part.Pricing.withoutCost(Money.of("50.00")),
                stockQuantity,
                minimumStock);
    }

    private static void assertCannotAddService(ServiceOrder serviceOrder) {
        assertThatThrownBy(() -> serviceOrder.addService(
                        new WorkshopService("Alignment", "Wheel alignment", Money.of("120.00"), 60), 1))
                .isInstanceOf(InvalidServiceOrderStatusTransitionException.class)
                .hasMessage("Service order items cannot be changed in current status");
    }

    @Test
    void shouldStartWithReceivedStatus() {
        ServiceOrder serviceOrder = serviceOrder();

        assertThat(serviceOrder.status()).isEqualTo(ServiceOrderStatus.RECEBIDA);
    }

    @Test
    void shouldStartDiagnosisCorrectly() {
        ServiceOrder serviceOrder = serviceOrder();

        serviceOrder.startDiagnosis();

        assertThat(serviceOrder.status()).isEqualTo(ServiceOrderStatus.EM_DIAGNOSTICO);
    }

    @Test
    void shouldGenerateBudgetWithServicesAndParts() {
        ServiceOrder serviceOrder = serviceOrderWithItems();

        Money total = serviceOrder.generateBudget();

        assertThat(total.value()).isEqualByComparingTo("400.00");
        assertThat(serviceOrder.totalAmount().value()).isEqualByComparingTo("400.00");
    }

    @Test
    void shouldChangeToWaitingApprovalAfterGeneratingBudget() {
        ServiceOrder serviceOrder = serviceOrderWithItems();

        serviceOrder.generateBudget();

        assertThat(serviceOrder.status()).isEqualTo(ServiceOrderStatus.AGUARDANDO_APROVACAO);
        assertThat(serviceOrder.budgetGeneratedAt()).isNotNull();
    }

    @Test
    void shouldApproveBudget() {
        ServiceOrder serviceOrder = serviceOrderWithItems();
        serviceOrder.generateBudget();

        serviceOrder.approveBudget();

        assertThat(serviceOrder.approvedAt()).isNotNull();
    }

    @Test
    void shouldNotStartExecutionWithoutApproval() {
        ServiceOrder serviceOrder = serviceOrderWithItems();
        serviceOrder.generateBudget();

        assertThatThrownBy(serviceOrder::startExecution)
                .isInstanceOf(DomainException.class)
                .hasMessage("Execution cannot start without budget approval");
    }

    @Test
    void shouldRejectAddingPartWhenStockIsUnavailable() {
        ServiceOrder serviceOrder = serviceOrder();
        Part part = part("OIL-LOW", 1, 1);

        assertThatThrownBy(() -> serviceOrder.addPart(part, 2))
                .isInstanceOf(DomainException.class)
                .hasMessage("Part stock is not available");
    }

    @Test
    void shouldRejectApprovalWhenBudgetTimestampIsMissing() {
        ServiceOrder serviceOrder = new ServiceOrder(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                ServiceOrderStatus.AGUARDANDO_APROVACAO,
                "Initial diagnostic notes",
                List.of(),
                List.of(),
                Money.zero(),
                LocalDateTime.now(),
                null,
                null,
                null,
                null,
                null);

        assertThatThrownBy(serviceOrder::approveBudget)
                .isInstanceOf(DomainException.class)
                .hasMessage("Budget must be generated before approval");
    }

    @Test
    void shouldNotChangeItemsAfterBudgetWasGenerated() {
        ServiceOrder serviceOrder = serviceOrderWithItems();
        serviceOrder.generateBudget();

        assertThatThrownBy(() -> serviceOrder.addService(
                        new WorkshopService("Alignment", "Wheel alignment", Money.of("120.00"), 60), 1))
                .isInstanceOf(InvalidServiceOrderStatusTransitionException.class)
                .hasMessage("Service order items cannot be changed in current status");
    }

    @Test
    void shouldNotChangeItemsWhenExecutionOrDeliveryFlowAlreadyStarted() {
        ServiceOrder inProgress = serviceOrderWithItems();
        inProgress.generateBudget();
        inProgress.approveBudget();
        inProgress.startExecution();
        assertCannotAddService(inProgress);

        ServiceOrder finished = serviceOrderWithItems();
        finished.generateBudget();
        finished.approveBudget();
        finished.startExecution();
        finished.finish();
        assertCannotAddService(finished);

        ServiceOrder delivered = serviceOrderWithItems();
        delivered.generateBudget();
        delivered.approveBudget();
        delivered.startExecution();
        delivered.finish();
        delivered.deliver();
        assertCannotAddService(delivered);
    }

    @Test
    void shouldNotFinishWithoutBeingInProgress() {
        ServiceOrder serviceOrder = serviceOrderWithItems();
        serviceOrder.generateBudget();
        serviceOrder.approveBudget();

        assertThatThrownBy(serviceOrder::finish)
                .isInstanceOf(DomainException.class)
                .hasMessage("Service order can only be finished while in progress");
    }

    @Test
    void shouldNotDeliverWithoutBeingFinished() {
        ServiceOrder serviceOrder = serviceOrderWithItems();
        serviceOrder.generateBudget();
        serviceOrder.approveBudget();
        serviceOrder.startExecution();

        assertThatThrownBy(serviceOrder::deliver)
                .isInstanceOf(DomainException.class)
                .hasMessage("Service order can only be delivered after finished");
    }

    @Test
    void shouldFollowCompleteStatusFlow() {
        ServiceOrder serviceOrder = serviceOrderWithItems();

        serviceOrder.startDiagnosis();
        serviceOrder.generateBudget();
        serviceOrder.approveBudget();
        serviceOrder.startExecution();
        serviceOrder.finish();
        serviceOrder.deliver();

        assertThat(serviceOrder.status()).isEqualTo(ServiceOrderStatus.ENTREGUE);
        assertThat(serviceOrder.budgetGeneratedAt()).isNotNull();
        assertThat(serviceOrder.approvedAt()).isNotNull();
        assertThat(serviceOrder.startedAt()).isNotNull();
        assertThat(serviceOrder.finishedAt()).isNotNull();
        assertThat(serviceOrder.deliveredAt()).isNotNull();
    }

    @Test
    void shouldRejectBlankNamesInServiceOrderItems() {
        assertThatThrownBy(() -> new ServiceOrder.ServiceOrderService(UUID.randomUUID(), "   ", 1, Money.of("10.00")))
                .isInstanceOf(DomainException.class)
                .hasMessage("Service name is required");
        assertThatThrownBy(
                        () -> new ServiceOrder.ServiceOrderPart(UUID.randomUUID(), "Part", "   ", 1, Money.of("10.00")))
                .isInstanceOf(DomainException.class)
                .hasMessage("SKU is required");
    }
}
