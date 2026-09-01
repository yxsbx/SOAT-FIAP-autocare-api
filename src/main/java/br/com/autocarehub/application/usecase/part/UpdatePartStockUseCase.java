package br.com.autocarehub.application.usecase.part;

import br.com.autocarehub.application.exception.ResourceNotFoundException;
import br.com.autocarehub.application.port.out.PartRepository;
import br.com.autocarehub.domain.model.Part;
import java.util.UUID;

public class UpdatePartStockUseCase {

    private final PartRepository partRepository;

    public UpdatePartStockUseCase(PartRepository partRepository) {
        this.partRepository = partRepository;
    }

    public Part execute(Command command) {
        Part part = partRepository
                .findById(command.partId())
                .orElseThrow(() -> new ResourceNotFoundException("Part not found"));
        int difference = command.stockQuantity() - part.stockQuantity();
        if (difference > 0) {
            part.increaseStock(difference);
        }
        if (difference < 0) {
            part.reduceStock(Math.abs(difference));
        }
        return partRepository.save(part);
    }

    public record Command(UUID partId, int stockQuantity) {}
}
