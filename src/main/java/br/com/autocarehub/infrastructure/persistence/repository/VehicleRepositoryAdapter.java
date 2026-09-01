package br.com.autocarehub.infrastructure.persistence.repository;

import br.com.autocarehub.application.port.out.VehicleRepository;
import br.com.autocarehub.domain.model.Vehicle;
import br.com.autocarehub.infrastructure.persistence.mapper.VehicleJpaMapper;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class VehicleRepositoryAdapter implements VehicleRepository {

    private final VehicleJpaRepository vehicleJpaRepository;

    public VehicleRepositoryAdapter(VehicleJpaRepository vehicleJpaRepository) {
        this.vehicleJpaRepository = vehicleJpaRepository;
    }

    @Override
    public Vehicle save(Vehicle vehicle) {
        return VehicleJpaMapper.toDomain(vehicleJpaRepository.save(VehicleJpaMapper.toEntity(vehicle)));
    }

    @Override
    public Optional<Vehicle> findById(UUID id) {
        return vehicleJpaRepository.findById(id).map(VehicleJpaMapper::toDomain);
    }

    @Override
    public List<Vehicle> findAll() {
        return vehicleJpaRepository.findAll().stream()
                .map(VehicleJpaMapper::toDomain)
                .toList();
    }

    @Override
    public List<Vehicle> findByCustomerId(UUID customerId) {
        return vehicleJpaRepository.findByCustomerId(customerId).stream()
                .map(VehicleJpaMapper::toDomain)
                .toList();
    }
}
