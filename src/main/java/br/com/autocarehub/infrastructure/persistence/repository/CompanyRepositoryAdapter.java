package br.com.autocarehub.infrastructure.persistence.repository;

import br.com.autocarehub.application.port.out.CompanyRepository;
import br.com.autocarehub.domain.model.Company;
import br.com.autocarehub.infrastructure.persistence.mapper.CompanyJpaMapper;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class CompanyRepositoryAdapter implements CompanyRepository {

    private final CompanyJpaRepository companyJpaRepository;

    public CompanyRepositoryAdapter(CompanyJpaRepository companyJpaRepository) {
        this.companyJpaRepository = companyJpaRepository;
    }

    @Override
    public Company save(Company company) {
        return CompanyJpaMapper.toDomain(companyJpaRepository.save(CompanyJpaMapper.toEntity(company)));
    }

    @Override
    public Optional<Company> findById(UUID id) {
        return companyJpaRepository.findById(id).map(CompanyJpaMapper::toDomain);
    }

    @Override
    public Optional<Company> findByName(String name) {
        return companyJpaRepository.findByName(name).map(CompanyJpaMapper::toDomain);
    }

    @Override
    public List<Company> findAll() {
        return companyJpaRepository.findAll().stream()
                .map(CompanyJpaMapper::toDomain)
                .toList();
    }
}
