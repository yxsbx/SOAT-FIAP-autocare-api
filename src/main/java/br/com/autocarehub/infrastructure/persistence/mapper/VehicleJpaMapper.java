package br.com.autocarehub.infrastructure.persistence.mapper;

import br.com.autocarehub.domain.model.Vehicle;
import br.com.autocarehub.domain.valueobject.Plate;
import br.com.autocarehub.infrastructure.persistence.entity.VehicleJpaEntity;

public final class VehicleJpaMapper {

    private VehicleJpaMapper() {}

    public static VehicleJpaEntity toEntity(Vehicle vehicle) {
        VehicleJpaEntity entity = new VehicleJpaEntity();
        entity.setId(vehicle.id());
        entity.setCustomerId(vehicle.customerId());
        entity.setPlate(vehicle.plate().value());
        entity.setBrand(vehicle.brand());
        entity.setModel(vehicle.model());
        entity.setYear(vehicle.year());
        entity.setMileage(vehicle.mileage());
        entity.setActive(vehicle.active());
        return entity;
    }

    public static Vehicle toDomain(VehicleJpaEntity entity) {
        return new Vehicle(
                entity.getId(),
                entity.getCustomerId(),
                new Plate(entity.getPlate()),
                entity.getBrand(),
                entity.getModel(),
                entity.getYear(),
                entity.getMileage(),
                entity.isActive());
    }
}
