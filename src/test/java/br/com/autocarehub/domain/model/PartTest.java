package br.com.autocarehub.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.autocarehub.domain.exception.DomainException;
import br.com.autocarehub.domain.valueobject.Money;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PartTest {

    private static Part part() {
        return part("OIL-001", Money.zero(), Money.of("50.00"), 10);
    }

    private static Part part(String sku, Money costPrice, Money unitPrice, int stockQuantity) {
        return Part.create(
                catalog("Oil filter", "Oil filter", sku, "Bosch"),
                new Part.Pricing(costPrice, unitPrice),
                stockQuantity,
                2);
    }

    private static Part.CatalogData catalog(String name, String description, String sku, String brand) {
        return new Part.CatalogData(name, description, sku, "Filters", null, brand);
    }

    private static Part restore(
            UUID id,
            Part.CatalogData catalogData,
            Money costPrice,
            Money unitPrice,
            int stockQuantity,
            int reservedQuantity,
            int minimumStock,
            LocalDateTime reservationExpiresAt) {
        return Part.restore(
                id,
                catalogData,
                new Part.Pricing(costPrice, unitPrice),
                new Part.StockState(stockQuantity, reservedQuantity, minimumStock, 3, reservationExpiresAt),
                Part.ActivationStatus.ACTIVE);
    }

    @Test
    void shouldNotAllowNegativeStock() {
        assertThatThrownBy(() -> part("OIL-001", Money.zero(), Money.of("50.00"), -1))
                .isInstanceOf(DomainException.class)
                .hasMessage("Stock cannot be negative");
    }

    @Test
    void shouldReduceStockWhenQuantityIsAvailable() {
        Part part = part();

        part.reduceStock(3);

        assertThat(part.stockQuantity()).isEqualTo(7);
    }

    @Test
    void shouldFailWhenReducingMoreThanAvailableStock() {
        Part part = part();

        assertThatThrownBy(() -> part.reduceStock(11))
                .isInstanceOf(DomainException.class)
                .hasMessage("Insufficient stock");
    }

    @Test
    void shouldIncreaseStock() {
        Part part = part();

        part.increaseStock(5);

        assertThat(part.stockQuantity()).isEqualTo(15);
    }

    @Test
    void shouldCommitReservedStockAndExtraAvailableStock() {
        Part part = part();
        part.reserveStock(3);

        part.commitReservedStock(5);

        assertThat(part.stockQuantity()).isEqualTo(5);
        assertThat(part.reservedQuantity()).isZero();
        assertThat(part.availableQuantity()).isEqualTo(5);
    }

    @Test
    void shouldRejectCommitGreaterThanAvailableAndReservedStock() {
        Part part = part();
        part.reserveStock(3);

        assertThatThrownBy(() -> part.commitReservedStock(11))
                .isInstanceOf(DomainException.class)
                .hasMessage("Insufficient stock");
    }

    @Test
    void shouldUpdatePartData() {
        Part part = part();

        part.update("Air filter", "Filtro de ar do motor", "AIR-001", "Filters", "Air", "Mann", Money.of("80.00"), 3);

        assertThat(part.name()).isEqualTo("Air filter");
        assertThat(part.description()).isEqualTo("Filtro de ar do motor");
        assertThat(part.sku()).isEqualTo("AIR-001");
        assertThat(part.category()).isEqualTo("Filters");
        assertThat(part.subcategory()).isEqualTo("Air");
        assertThat(part.brand()).isEqualTo("Mann");
        assertThat(part.unitPrice().value()).isEqualByComparingTo("80.00");
        assertThat(part.minimumStock()).isEqualTo(3);
    }

    @Test
    void shouldReportAvailableStock() {
        Part part = part();

        assertThat(part.hasAvailableStock(10)).isTrue();
        assertThat(part.hasAvailableStock(11)).isFalse();
        assertThat(part.hasAvailableStock(0)).isFalse();
    }

    @Test
    void shouldActivateAndDeactivate() {
        Part part = part();

        part.deactivate();
        assertThat(part.active()).isFalse();

        part.activate();
        assertThat(part.active()).isTrue();
    }

    @Test
    void shouldRejectInvalidStockQuantities() {
        Part part = part();

        assertThatThrownBy(() -> part.increaseStock(0))
                .isInstanceOf(DomainException.class)
                .hasMessage("Quantity must be greater than zero");
        assertThatThrownBy(() -> part.reduceStock(0))
                .isInstanceOf(DomainException.class)
                .hasMessage("Quantity must be greater than zero");
    }

    @Test
    void shouldRejectInvalidPartUpdate() {
        Part part = part();

        assertThatThrownBy(() -> part.update("Air filter", "AIR-001", "Filters", null, "Mann", Money.zero(), 3))
                .isInstanceOf(DomainException.class)
                .hasMessage("Unit price must be greater than zero");
        assertThatThrownBy(() -> part.update("Air filter", "AIR-001", "Filters", null, "Mann", Money.of("80.00"), -1))
                .isInstanceOf(DomainException.class)
                .hasMessage("Minimum stock cannot be negative");
    }

    @Test
    void shouldCoverPartConstructorAndUpdateOverloads() {
        UUID id = UUID.randomUUID();
        Part first = part("OIL-002", Money.of("25.00"), Money.of("50.00"), 10);
        Part second = Part.restore(
                id,
                catalog("Cabin filter", "Cabin filter", "CAB-001", "Mann"),
                Part.Pricing.withoutCost(Money.of("60.00")),
                Part.StockState.initial(6, 2),
                Part.ActivationStatus.ACTIVE);
        Part third = Part.create(
                catalog("Air filter", "Air filter description", "AIR-001", "Mann"),
                Part.Pricing.withoutCost(Money.of("70.00")),
                8,
                2);

        first.update("Oil filter premium", "OIL-003", "Filters", null, "Bosch", Money.of("65.00"), 3);
        second.update(
                "Cabin filter premium", "CAB-002", "Filters", null, "Mann", Money.of("30.00"), Money.of("80.00"), 4);

        assertThat(first.description()).isEqualTo("Oil filter premium");
        assertThat(second.id()).isEqualTo(id);
        assertThat(second.costPrice().value()).isEqualByComparingTo("30.00");
        assertThat(third.description()).isEqualTo("Air filter description");
    }

    @Test
    void shouldRejectAdditionalInvalidPartStates() {
        assertThatThrownBy(() -> restore(
                        UUID.randomUUID(),
                        catalog("Oil filter", "Oil filter", "OIL-004", "Bosch"),
                        Money.of("25.00"),
                        Money.of("50.00"),
                        2,
                        3,
                        1,
                        null))
                .isInstanceOf(DomainException.class)
                .hasMessage("Reserved stock cannot be greater than stock");

        Part part = part();
        assertThatThrownBy(() -> part.reserveStock(0))
                .isInstanceOf(DomainException.class)
                .hasMessage("Quantity must be greater than zero");
        assertThatThrownBy(() -> part.commitReservedStock(0))
                .isInstanceOf(DomainException.class)
                .hasMessage("Quantity must be greater than zero");
        assertThatThrownBy(() -> part.releaseReservedStock(0))
                .isInstanceOf(DomainException.class)
                .hasMessage("Quantity must be greater than zero");
        assertThatThrownBy(() -> part.configureReservationDays(0))
                .isInstanceOf(DomainException.class)
                .hasMessage("Reservation days must be greater than zero");
    }

    @Test
    void shouldReleaseExpiredReservation() {
        Part part = restore(
                UUID.randomUUID(),
                catalog("Oil filter", "Oil filter", "OIL-005", "Bosch"),
                Money.of("25.00"),
                Money.of("50.00"),
                10,
                4,
                2,
                LocalDateTime.now().minusDays(1));

        part.releaseExpiredReservation();

        assertThat(part.reservedQuantity()).isZero();
        assertThat(part.reservationExpiresAt()).isNull();
    }
}
