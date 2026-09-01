package br.com.autocarehub.application.usecase.demo;

import br.com.autocarehub.application.port.out.DemoLeadRepository;
import br.com.autocarehub.domain.enums.DocumentType;
import br.com.autocarehub.domain.exception.DomainException;
import br.com.autocarehub.domain.model.DemoLead;
import br.com.autocarehub.domain.valueobject.Document;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

public class RegisterDemoLeadUseCase {

    private final DemoLeadRepository repository;

    public RegisterDemoLeadUseCase(DemoLeadRepository repository) {
        this.repository = repository;
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    public DemoLead execute(Command command) {
        Document cnpj = Document.from(command.cnpj());
        if (cnpj.type() != DocumentType.CNPJ) {
            throw new DomainException("Demo lead document must be CNPJ");
        }
        DemoLead demoLead = new DemoLead(
                UUID.randomUUID(),
                command.contactName().trim(),
                command.companyName().trim(),
                command.demoProfile().trim(),
                command.email().trim().toLowerCase(Locale.ROOT),
                command.phone().trim(),
                cnpj.value(),
                normalize(command.city()),
                normalize(command.message()),
                LocalDateTime.now());

        return repository.save(demoLead);
    }

    public record Command(
            String contactName,
            String companyName,
            String demoProfile,
            String email,
            String phone,
            String cnpj,
            String city,
            String message) {}
}
