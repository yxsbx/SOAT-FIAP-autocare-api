package br.com.autocarehub.application.port.out;

import br.com.autocarehub.domain.model.Company;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CompanyRepository {

    Company save(Company company);

    Optional<Company> findById(UUID id);

    Optional<Company> findByName(String name);

    List<Company> findAll();
}
