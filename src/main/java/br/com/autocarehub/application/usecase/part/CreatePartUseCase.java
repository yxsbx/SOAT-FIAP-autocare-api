package br.com.autocarehub.application.usecase.part;

import br.com.autocarehub.application.port.out.PartRepository;
import br.com.autocarehub.domain.model.Part;
import br.com.autocarehub.domain.valueobject.Money;

public class CreatePartUseCase {

    private final PartRepository partRepository;

    public CreatePartUseCase(PartRepository partRepository) {
        this.partRepository = partRepository;
    }

    public Part execute(Command command) {
        Part part = Part.create(
                new Part.CatalogData(
                        command.name(),
                        command.description(),
                        command.sku(),
                        command.category(),
                        command.subcategory(),
                        command.brand()),
                new Part.Pricing(command.costPrice(), command.unitPrice()),
                command.stockQuantity(),
                command.minimumStock());
        return partRepository.save(part);
    }

    public record Command(
            String name,
            String description,
            String sku,
            String category,
            String subcategory,
            String brand,
            Money costPrice,
            Money unitPrice,
            int stockQuantity,
            int minimumStock) {}
}
