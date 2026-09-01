package br.com.autocarehub.application.usecase.part;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.autocarehub.application.port.out.PartRepository;
import br.com.autocarehub.application.port.out.StockMovementRepository;
import br.com.autocarehub.domain.exception.DomainException;
import br.com.autocarehub.domain.model.Part;
import br.com.autocarehub.domain.valueobject.Money;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RegisterPartStockMovementUseCaseTest {

    private final InMemoryPartRepository partRepository = new InMemoryPartRepository();
    private final InMemoryStockMovementRepository stockMovementRepository = new InMemoryStockMovementRepository();

    private static Part part() {
        return Part.create(
                new Part.CatalogData(
                        "Filtro de oleo", "Filtro de oleo do motor", "OIL-001", "Filtros", "Oleo", "Bosch"),
                new Part.Pricing(Money.of("25.00"), Money.of("50.00")),
                10,
                2);
    }

    @Test
    void shouldRegisterStockEntry() {
        Part part = partRepository.save(part());
        RegisterPartStockMovementUseCase useCase = useCase();

        Part updated = useCase.execute(new RegisterPartStockMovementUseCase.Command(
                part.id(),
                RegisterPartStockMovementUseCase.MovementType.ENTRY,
                5,
                Money.of("30.00"),
                Money.of("50.00"),
                "Entrada de fornecedor"));

        assertThat(updated.stockQuantity()).isEqualTo(15);
        assertThat(stockMovementRepository.movements()).hasSize(1);
        assertThat(stockMovementRepository.movements().getFirst().movementType())
                .isEqualTo("ENTRY");
    }

    @Test
    void shouldRegisterStockExit() {
        Part part = partRepository.save(part());
        RegisterPartStockMovementUseCase useCase = useCase();

        Part updated = useCase.execute(new RegisterPartStockMovementUseCase.Command(
                part.id(), RegisterPartStockMovementUseCase.MovementType.EXIT, 4, null, null, "Uso interno"));

        assertThat(updated.stockQuantity()).isEqualTo(6);
        assertThat(stockMovementRepository.movements()).hasSize(1);
        assertThat(stockMovementRepository.movements().getFirst().movementType())
                .isEqualTo("EXIT");
    }

    @Test
    void shouldRejectExitGreaterThanAvailableStock() {
        Part part = partRepository.save(part());
        RegisterPartStockMovementUseCase useCase = useCase();

        assertThatThrownBy(() -> useCase.execute(new RegisterPartStockMovementUseCase.Command(
                        part.id(),
                        RegisterPartStockMovementUseCase.MovementType.EXIT,
                        11,
                        null,
                        null,
                        "Saida invalida")))
                .isInstanceOf(DomainException.class)
                .hasMessage("Insufficient stock");
    }

    private RegisterPartStockMovementUseCase useCase() {
        return new RegisterPartStockMovementUseCase(partRepository, stockMovementRepository);
    }

    private static class InMemoryPartRepository implements PartRepository {

        private final Map<UUID, Part> parts = new LinkedHashMap<>();

        @Override
        public Part save(Part part) {
            parts.put(part.id(), part);
            return part;
        }

        @Override
        public Optional<Part> findById(UUID id) {
            return Optional.ofNullable(parts.get(id));
        }

        @Override
        public List<Part> findAll() {
            return List.copyOf(parts.values());
        }
    }

    private static class InMemoryStockMovementRepository implements StockMovementRepository {

        private final List<Movement> movements = new ArrayList<>();

        @Override
        public void register(
                UUID partId,
                String movementType,
                int quantity,
                BigDecimal unitCost,
                BigDecimal unitPrice,
                String reason) {
            movements.add(new Movement(partId, movementType, quantity, unitCost, unitPrice, reason));
        }

        private List<Movement> movements() {
            return movements;
        }
    }

    private record Movement(
            UUID partId, String movementType, int quantity, BigDecimal unitCost, BigDecimal unitPrice, String reason) {}
}
