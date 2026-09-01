package br.com.autocarehub.application.usecase.part;

import br.com.autocarehub.application.exception.ResourceNotFoundException;
import br.com.autocarehub.application.port.out.PartRepository;
import br.com.autocarehub.domain.model.Part;
import java.util.UUID;

public class ReleasePartReservationUseCase {

    private final PartRepository partRepository;

    public ReleasePartReservationUseCase(PartRepository partRepository) {
        this.partRepository = partRepository;
    }

    public Part execute(Command command) {
        Part part = partRepository
                .findById(command.partId())
                .orElseThrow(() -> new ResourceNotFoundException("Part not found"));
        part.releaseReservedStock(command.quantity());
        return partRepository.save(part);
    }

    public record Command(UUID partId, int quantity) {}
}
