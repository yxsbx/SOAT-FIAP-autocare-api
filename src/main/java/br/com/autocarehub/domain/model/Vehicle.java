package br.com.autocarehub.domain.model;

import br.com.autocarehub.domain.exception.DomainException;
import br.com.autocarehub.domain.service.DomainValidation;
import br.com.autocarehub.domain.valueobject.Plate;
import java.util.Objects;
import java.util.UUID;

public class Vehicle {

    private static final int BRAND_MAX_LENGTH = 60;
    private static final int MODEL_MAX_LENGTH = 80;

    private final UUID id;
    private final UUID customerId;
    private Plate plate;
    private String brand;
    private String model;
    private int year;
    private int mileage;
    private boolean active;

    public Vehicle(UUID customerId, Plate plate, String brand, String model, int year, int mileage) {
        this(UUID.randomUUID(), customerId, plate, brand, model, year, mileage, true);
    }

    public Vehicle(
            UUID id, UUID customerId, Plate plate, String brand, String model, int year, int mileage, boolean active) {
        this.id = Objects.requireNonNull(id, "id is required");
        this.customerId = Objects.requireNonNull(customerId, "customerId is required");
        this.plate = Objects.requireNonNull(plate, "plate is required");
        this.brand = DomainValidation.requireText(brand, "Brand is required", BRAND_MAX_LENGTH);
        this.model = DomainValidation.requireText(model, "Model is required", MODEL_MAX_LENGTH);
        this.year = DomainValidation.requireVehicleYear(year);
        this.mileage = requireMileage(mileage);
        this.active = active;
    }

    private static int requireMileage(int value) {
        if (value < 0) {
            throw new DomainException("Mileage cannot be negative");
        }
        return value;
    }

    public void update(Plate newPlate, String newBrand, String newModel, int newYear, int newMileage) {
        plate = Objects.requireNonNull(newPlate, "plate is required");
        brand = DomainValidation.requireText(newBrand, "Brand is required", BRAND_MAX_LENGTH);
        model = DomainValidation.requireText(newModel, "Model is required", MODEL_MAX_LENGTH);
        year = DomainValidation.requireVehicleYear(newYear);
        mileage = requireMileage(newMileage);
    }

    public void updateMileage(int newMileage) {
        if (newMileage < mileage) {
            throw new DomainException("Mileage cannot decrease");
        }
        mileage = requireMileage(newMileage);
    }

    public void activate() {
        active = true;
    }

    public void deactivate() {
        active = false;
    }

    public UUID id() {
        return id;
    }

    public UUID customerId() {
        return customerId;
    }

    public Plate plate() {
        return plate;
    }

    public String brand() {
        return brand;
    }

    public String model() {
        return model;
    }

    public int year() {
        return year;
    }

    public int mileage() {
        return mileage;
    }

    public boolean active() {
        return active;
    }
}
