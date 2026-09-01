package br.com.autocarehub.application.usecase.demo;

import br.com.autocarehub.application.port.out.DemoLeadRepository;
import br.com.autocarehub.domain.model.DemoLead;
import java.util.List;

public class ListDemoLeadsUseCase {

    private final DemoLeadRepository repository;

    public ListDemoLeadsUseCase(DemoLeadRepository repository) {
        this.repository = repository;
    }

    public List<DemoLead> execute() {
        return repository.findAll();
    }
}
