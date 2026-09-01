package br.com.autocarehub.application.usecase.part;

import br.com.autocarehub.application.exception.ResourceNotFoundException;
import br.com.autocarehub.application.port.out.PartRepository;
import br.com.autocarehub.domain.model.Part;
import java.util.UUID;

public class ConfigurePartReservationUseCase {

    private final PartRepository partRepository;

    public ConfigurePartReservationUseCase(PartRepository partRepository) {
        this.partRepository = partRepository;
    }

    public Part execute(Command command) {
        Part part = partRepository
                .findById(command.partId())
                .orElseThrow(() -> new ResourceNotFoundException("Part not found"));
        part.configureReservationDays(command.reservationDays());
        return partRepository.save(part);
    }

    public record Command(UUID partId, int reservationDays) {}
}
