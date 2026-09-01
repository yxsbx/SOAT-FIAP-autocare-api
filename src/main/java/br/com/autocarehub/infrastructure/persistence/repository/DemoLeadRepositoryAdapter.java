package br.com.autocarehub.infrastructure.persistence.repository;

import br.com.autocarehub.application.port.out.DemoLeadRepository;
import br.com.autocarehub.domain.model.DemoLead;
import br.com.autocarehub.infrastructure.persistence.entity.DemoLeadJpaEntity;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

@Repository
public class DemoLeadRepositoryAdapter implements DemoLeadRepository {

    private final DemoLeadJpaRepository repository;

    public DemoLeadRepositoryAdapter(DemoLeadJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public DemoLead save(DemoLead demoLead) {
        return toDomain(repository.save(toEntity(demoLead)));
    }

    @Override
    public List<DemoLead> findAll() {
        return repository.findAll(Sort.by(Sort.Direction.DESC, "createdAt")).stream()
                .map(this::toDomain)
                .toList();
    }

    private DemoLeadJpaEntity toEntity(DemoLead demoLead) {
        DemoLeadJpaEntity entity = new DemoLeadJpaEntity();
        entity.setId(demoLead.id());
        entity.setContactName(demoLead.contactName());
        entity.setCompanyName(demoLead.companyName());
        entity.setDemoProfile(demoLead.demoProfile());
        entity.setEmail(demoLead.email());
        entity.setPhone(demoLead.phone());
        entity.setCnpj(demoLead.cnpj());
        entity.setCity(demoLead.city());
        entity.setMessage(demoLead.message());
        entity.setCreatedAt(demoLead.createdAt());
        return entity;
    }

    private DemoLead toDomain(DemoLeadJpaEntity entity) {
        return new DemoLead(
                entity.getId(),
                entity.getContactName(),
                entity.getCompanyName(),
                entity.getDemoProfile(),
                entity.getEmail(),
                entity.getPhone(),
                entity.getCnpj(),
                entity.getCity(),
                entity.getMessage(),
                entity.getCreatedAt());
    }
}
