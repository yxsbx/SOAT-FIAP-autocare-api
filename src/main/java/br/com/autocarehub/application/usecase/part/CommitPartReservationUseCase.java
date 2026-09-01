package br.com.autocarehub.application.usecase.part;

import br.com.autocarehub.application.exception.ResourceNotFoundException;
import br.com.autocarehub.application.port.out.PartRepository;
import br.com.autocarehub.application.port.out.StockMovementRepository;
import br.com.autocarehub.domain.model.Part;
import java.util.UUID;

public class CommitPartReservationUseCase {

    private final PartRepository partRepository;
    private final StockMovementRepository stockMovementRepository;

    public CommitPartReservationUseCase(
            PartRepository partRepository, StockMovementRepository stockMovementRepository) {
        this.partRepository = partRepository;
        this.stockMovementRepository = stockMovementRepository;
    }

    public Part execute(Command command) {
        Part part = partRepository
                .findById(command.partId())
                .orElseThrow(() -> new ResourceNotFoundException("Part not found"));
        part.commitReservedStock(command.quantity());
        Part saved = partRepository.save(part);
        stockMovementRepository.register(
                command.partId(),
                "SALE",
                command.quantity(),
                part.costPrice().value(),
                part.unitPrice().value(),
                command.reason());
        return saved;
    }

    public record Command(UUID partId, int quantity, String reason) {}
}
