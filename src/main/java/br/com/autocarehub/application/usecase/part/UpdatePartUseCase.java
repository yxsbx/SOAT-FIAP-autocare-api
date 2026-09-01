package br.com.autocarehub.application.usecase.part;

import br.com.autocarehub.application.exception.ResourceNotFoundException;
import br.com.autocarehub.application.port.out.PartRepository;
import br.com.autocarehub.domain.model.Part;
import br.com.autocarehub.domain.valueobject.Money;
import java.util.UUID;

public class UpdatePartUseCase {

    private final PartRepository partRepository;

    public UpdatePartUseCase(PartRepository partRepository) {
        this.partRepository = partRepository;
    }

    public Part execute(Command command) {
        Part part = partRepository
                .findById(command.partId())
                .orElseThrow(() -> new ResourceNotFoundException("Part not found"));
        part.update(
                command.name(),
                command.description(),
                command.sku(),
                command.category(),
                command.subcategory(),
                command.brand(),
                command.costPrice(),
                command.unitPrice(),
                command.minimumStock());
        int stockDifference = command.stockQuantity() - part.stockQuantity();
        if (stockDifference > 0) {
            part.increaseStock(stockDifference);
        }
        if (stockDifference < 0) {
            part.reduceStock(Math.abs(stockDifference));
        }
        if (command.active()) {
            part.activate();
        } else {
            part.deactivate();
        }
        return partRepository.save(part);
    }

    public record Command(
            UUID partId,
            String name,
            String description,
            String sku,
            String category,
            String subcategory,
            String brand,
            Money costPrice,
            Money unitPrice,
            int stockQuantity,
            int minimumStock,
            boolean active) {}
}
