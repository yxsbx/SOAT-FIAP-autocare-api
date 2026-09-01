package br.com.autocarehub.infrastructure.persistence.mapper;

import br.com.autocarehub.domain.model.Company;
import br.com.autocarehub.infrastructure.persistence.entity.CompanyJpaEntity;

public final class CompanyJpaMapper {

    private CompanyJpaMapper() {}

    public static Company toDomain(CompanyJpaEntity entity) {
        return new Company(
                entity.getId(), entity.getName(), entity.getType(), entity.isActive(), entity.getCreatedAt());
    }

    public static CompanyJpaEntity toEntity(Company company) {
        CompanyJpaEntity entity = new CompanyJpaEntity();
        entity.setId(company.id());
        entity.setName(company.name());
        entity.setType(company.type());
        entity.setActive(company.active());
        entity.setCreatedAt(company.createdAt());
        return entity;
    }
}
