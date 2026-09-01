package br.com.autocarehub.application.usecase.part;

import br.com.autocarehub.application.exception.ResourceNotFoundException;
import br.com.autocarehub.application.port.out.PartRepository;
import br.com.autocarehub.domain.model.Part;
import java.util.UUID;

public class DeletePartUseCase {

    private final PartRepository partRepository;

    public DeletePartUseCase(PartRepository partRepository) {
        this.partRepository = partRepository;
    }

    public void execute(UUID partId) {
        Part part = partRepository.findById(partId).orElseThrow(() -> new ResourceNotFoundException("Part not found"));
        part.deactivate();
        partRepository.save(part);
    }
}
