package br.com.autocarehub.application.usecase.part;

import br.com.autocarehub.application.port.out.PartRepository;
import br.com.autocarehub.domain.model.Part;
import java.util.List;

public class ListPartsUseCase {

    private final PartRepository partRepository;

    public ListPartsUseCase(PartRepository partRepository) {
        this.partRepository = partRepository;
    }

    public List<Part> execute() {
        return partRepository.findAll();
    }

    public List<Part> execute(Query query) {
        return partRepository.findAll().stream()
                .filter(part -> query.active() == null || part.active() == query.active())
                .filter(part -> query.lowStock() == null
                        || !query.lowStock()
                        || part.availableQuantity() <= part.minimumStock())
                .toList();
    }

    public record Query(Boolean active, Boolean lowStock) {}
}
