package br.com.autocarehub.application.port.out;

import br.com.autocarehub.domain.model.Part;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PartRepository {

    Part save(Part part);

    Optional<Part> findById(UUID id);

    List<Part> findAll();
}
