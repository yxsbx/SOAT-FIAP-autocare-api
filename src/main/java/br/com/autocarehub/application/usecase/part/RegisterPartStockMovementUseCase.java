package br.com.autocarehub.application.usecase.part;

import br.com.autocarehub.application.exception.ResourceNotFoundException;
import br.com.autocarehub.application.port.out.PartRepository;
import br.com.autocarehub.application.port.out.StockMovementRepository;
import br.com.autocarehub.domain.model.Part;
import br.com.autocarehub.domain.valueobject.Money;
import java.util.UUID;

public class RegisterPartStockMovementUseCase {

    private final PartRepository partRepository;
    private final StockMovementRepository stockMovementRepository;

    public RegisterPartStockMovementUseCase(
            PartRepository partRepository, StockMovementRepository stockMovementRepository) {
        this.partRepository = partRepository;
        this.stockMovementRepository = stockMovementRepository;
    }

    public Part execute(Command command) {
        Part part = partRepository
                .findById(command.partId())
                .orElseThrow(() -> new ResourceNotFoundException("Part not found"));
        switch (command.type()) {
            case ENTRY -> part.increaseStock(command.quantity());
            case EXIT, SALE -> part.reduceStock(command.quantity());
        }
        Part saved = partRepository.save(part);
        stockMovementRepository.register(
                command.partId(),
                command.type().name(),
                command.quantity(),
                command.unitCost() == null
                        ? part.costPrice().value()
                        : command.unitCost().value(),
                command.unitPrice() == null
                        ? part.unitPrice().value()
                        : command.unitPrice().value(),
                command.reason());
        return saved;
    }

    public enum MovementType {
        ENTRY,
        EXIT,
        SALE
    }

    public record Command(
            UUID partId, MovementType type, int quantity, Money unitCost, Money unitPrice, String reason) {}
}
