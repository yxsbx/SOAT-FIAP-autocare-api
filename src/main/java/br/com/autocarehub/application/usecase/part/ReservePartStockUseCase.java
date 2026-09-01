package br.com.autocarehub.application.usecase.part;

import br.com.autocarehub.application.exception.ResourceNotFoundException;
import br.com.autocarehub.application.port.out.PartRepository;
import br.com.autocarehub.domain.model.Part;
import java.util.UUID;

public class ReservePartStockUseCase {

    private final PartRepository partRepository;

    public ReservePartStockUseCase(PartRepository partRepository) {
        this.partRepository = partRepository;
    }

    public Part execute(Command command) {
        Part part = partRepository
                .findById(command.partId())
                .orElseThrow(() -> new ResourceNotFoundException("Part not found"));
        part.reserveStock(command.quantity());
        return partRepository.save(part);
    }

    public record Command(UUID partId, int quantity) {}
}
