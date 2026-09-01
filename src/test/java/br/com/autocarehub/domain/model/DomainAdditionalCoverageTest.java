package br.com.autocarehub.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.autocarehub.domain.enums.ServiceOrderStatus;
import br.com.autocarehub.domain.enums.StockMovementType;
import br.com.autocarehub.domain.exception.DomainException;
import br.com.autocarehub.domain.service.DomainValidation;
import br.com.autocarehub.domain.valueobject.Address;
import br.com.autocarehub.domain.valueobject.Document;
import br.com.autocarehub.domain.valueobject.Money;
import br.com.autocarehub.domain.valueobject.Plate;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class DomainAdditionalCoverageTest {

    @Test
    void shouldCoverMoneyOperationsAndValidation() {
        Money money = Money.of(new BigDecimal("10.555"));

        assertThat(money.value()).isEqualByComparingTo("10.56");
        assertThat(Money.zero().isZero()).isTrue();
        assertThat(Money.zero().isZeroOrNegative()).isTrue();
        assertThat(money.add(Money.of("2.44")).value()).isEqualByComparingTo("13.00");
        assertThat(money.multiply(3).value()).isEqualByComparingTo("31.68");
        assertThat(money.compareTo(Money.of("9.00"))).isPositive();

        assertThatThrownBy(() -> Money.of("-1.00"))
                .isInstanceOf(DomainException.class)
                .hasMessage("Money cannot be negative");
        assertThatThrownBy(() -> money.multiply(-1))
                .isInstanceOf(DomainException.class)
                .hasMessage("Quantity cannot be negative");
    }

    @Test
    void shouldCoverDocumentAndPlateValidationBranches() {
        assertThat(Document.from("529.982.247-25").value()).isEqualTo("52998224725");
        assertThat(Document.from("11.222.333/0001-81").value()).isEqualTo("11222333000181");
        assertThat(new Plate("abc-1234").value()).isEqualTo("ABC1234");
        assertThat(new Plate("abc1d23").value()).isEqualTo("ABC1D23");

        assertThatThrownBy(() -> Document.from(null))
                .isInstanceOf(DomainException.class)
                .hasMessage("Document must be CPF or CNPJ");
        assertThatThrownBy(() -> Document.from("   "))
                .isInstanceOf(DomainException.class)
                .hasMessage("Document must be CPF or CNPJ");
        assertThatThrownBy(() -> Document.from("11111111111"))
                .isInstanceOf(DomainException.class)
                .hasMessage("Invalid document");
        assertThatThrownBy(() -> Document.from("11222333000180"))
                .isInstanceOf(DomainException.class)
                .hasMessage("Invalid document");
        assertThatThrownBy(() -> new Plate("ABC"))
                .isInstanceOf(DomainException.class)
                .hasMessage("Invalid plate");
    }

    @Test
    void shouldCoverStockMovementValidationAndEnumValues() {
        UUID partId = UUID.randomUUID();
        StockMovement movement = new StockMovement(
                partId,
                StockMovementType.ENTRY,
                2,
                Money.of("10.00"),
                Money.of("15.00"),
                " Compra ",
                LocalDateTime.now());

        assertThat(movement.partId()).isEqualTo(partId);
        assertThat(movement.type()).isEqualTo(StockMovementType.ENTRY);
        assertThat(movement.reason()).isEqualTo("Compra");
        assertThat(StockMovementType.valueOf("EXIT")).isEqualTo(StockMovementType.EXIT);
        assertThat(StockMovementType.values()).contains(StockMovementType.SALE);

        assertThatThrownBy(() -> new StockMovement(
                        partId,
                        StockMovementType.SALE,
                        0,
                        Money.of("10.00"),
                        Money.of("15.00"),
                        "Venda",
                        LocalDateTime.now()))
                .isInstanceOf(DomainException.class)
                .hasMessage("Quantity must be greater than zero");
    }

    @Test
    void shouldCoverServiceOrderStatusExternalCodeMapping() {
        assertThat(ServiceOrderStatus.fromExternalCode("RECEBIDA")).isEqualTo(ServiceOrderStatus.RECEBIDA);
        assertThat(ServiceOrderStatus.fromExternalCode("IN_PROGRESS")).isEqualTo(ServiceOrderStatus.EM_EXECUCAO);
        assertThat(ServiceOrderStatus.ENTREGUE.externalCode()).isEqualTo("DELIVERED");

        assertThatThrownBy(() -> ServiceOrderStatus.fromExternalCode("UNKNOWN"))
                .isInstanceOf(DomainException.class)
                .hasMessage("Invalid service order status");
    }

    @Test
    void shouldCoverAdditionalPartBranches() {
        Part part = Part.create(
                new Part.CatalogData(
                        "Filtro de oleo", "Filtro de oleo do motor", "OIL-EXTRA", "Filtros", "Oleo", "Bosch"),
                new Part.Pricing(Money.of("25.00"), Money.of("50.00")),
                4,
                4);

        assertThat(part.stockStatus()).isEqualTo("LOW_STOCK");

        part.reserveStock(2);
        assertThat(part.stockStatus()).isEqualTo("LOW_STOCK");

        part.releaseReservedStock(99);
        assertThat(part.reservedQuantity()).isZero();
        assertThat(part.reservationExpiresAt()).isNull();

        part.reduceStock(4);
        assertThat(part.stockStatus()).isEqualTo("OUT_OF_STOCK");

        part.deactivate();
        assertThat(part.stockStatus()).isEqualTo("INACTIVE");
        assertThat(part.costPrice().value()).isEqualByComparingTo("25.00");
        assertThat(part.subcategory()).isEqualTo("Oleo");
        assertThat(part.description()).isEqualTo("Filtro de oleo do motor");
    }

    @Test
    void shouldCoverBudgetItemAndPartConstructorValidationBranches() {
        BudgetItem item = new BudgetItem(UUID.randomUUID(), " Filtro ", 2, Money.of("10.00"));

        assertThat(item.description()).isEqualTo("Filtro");
        assertThat(item.totalPrice().value()).isEqualByComparingTo("20.00");

        assertThatThrownBy(() -> new BudgetItem(UUID.randomUUID(), "   ", 1, Money.of("10.00")))
                .isInstanceOf(DomainException.class)
                .hasMessage("Budget item description is required");
        assertThatThrownBy(() -> new BudgetItem(UUID.randomUUID(), "Filtro", 0, Money.of("10.00")))
                .isInstanceOf(DomainException.class)
                .hasMessage("Quantity must be greater than zero");
        assertThatThrownBy(() -> Part.create(
                        new Part.CatalogData("Filtro", "Filtro de oleo", "SKU-NEG", "Filtros", null, "Bosch"),
                        new Part.Pricing(Money.of("1.00"), Money.zero()),
                        1,
                        1))
                .isInstanceOf(DomainException.class)
                .hasMessage("Unit price must be greater than zero");
        assertThatThrownBy(() -> Part.restore(
                        UUID.randomUUID(),
                        new Part.CatalogData("Filtro", "Filtro de oleo", "SKU-RES", "Filtros", null, "Bosch"),
                        new Part.Pricing(Money.of("1.00"), Money.of("10.00")),
                        new Part.StockState(1, 2, 1, 3, null),
                        Part.ActivationStatus.ACTIVE))
                .isInstanceOf(DomainException.class)
                .hasMessage("Reserved stock cannot be greater than stock");
    }

    @Test
    void shouldCoverDomainValidationAndAddressBranches() {
        assertThat(DomainValidation.optionalText(null)).isNull();
        assertThat(DomainValidation.optionalText("   ")).isNull();
        assertThat(DomainValidation.optionalText("  valor  ")).isEqualTo("valor");
        assertThatThrownBy(() -> DomainValidation.optionalText("x".repeat(81)))
                .isInstanceOf(DomainException.class)
                .hasMessage("Text exceeds maximum length");
        assertThat(DomainValidation.requireText(" texto ", "required", 10)).isEqualTo("texto");
        assertThatThrownBy(() -> DomainValidation.requireText(" ", "required", 10))
                .isInstanceOf(DomainException.class)
                .hasMessage("required");
        assertThatThrownBy(() -> DomainValidation.requireText("texto longo", "required", 3))
                .isInstanceOf(DomainException.class)
                .hasMessage("Text exceeds maximum length");

        Address address = new Address("Rua A", "10", "Apto 1", "Centro", "São Paulo", "SP", "01001-000");

        assertThat(address.complement()).isEqualTo("Apto 1");
        assertThat(address.zipCode()).isEqualTo("01001000");
    }
}
