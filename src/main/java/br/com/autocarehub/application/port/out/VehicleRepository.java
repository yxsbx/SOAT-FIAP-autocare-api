package br.com.autocarehub.application.port.out;

import br.com.autocarehub.domain.model.Vehicle;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VehicleRepository {

    Vehicle save(Vehicle vehicle);

    Optional<Vehicle> findById(UUID id);

    List<Vehicle> findAll();

    List<Vehicle> findByCustomerId(UUID customerId);
}
