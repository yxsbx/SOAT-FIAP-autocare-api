package br.com.autocarehub.domain.model;

import br.com.autocarehub.domain.exception.DomainException;
import br.com.autocarehub.domain.service.DomainValidation;
import br.com.autocarehub.domain.valueobject.Money;
import java.util.Objects;
import java.util.UUID;

public class WorkshopService {

    private static final int NAME_MAX_LENGTH = 100;
    private static final int DESCRIPTION_MAX_LENGTH = 500;

    private final UUID id;
    private String name;
    private String description;
    private Money basePrice;
    private int estimatedTimeInMinutes;
    private boolean active;

    public WorkshopService(String name, String description, Money basePrice, int estimatedTimeInMinutes) {
        this(UUID.randomUUID(), name, description, basePrice, estimatedTimeInMinutes, true);
    }

    public WorkshopService(
            UUID id, String name, String description, Money basePrice, int estimatedTimeInMinutes, boolean active) {
        this.id = Objects.requireNonNull(id, "id is required");
        this.name = DomainValidation.requireText(name, "Name is required", NAME_MAX_LENGTH);
        this.description = DomainValidation.requireText(description, "Description is required", DESCRIPTION_MAX_LENGTH);
        this.basePrice = requirePositiveMoney(basePrice);
        this.estimatedTimeInMinutes = requireEstimatedTime(estimatedTimeInMinutes);
        this.active = active;
    }

    private static Money requirePositiveMoney(Money money) {
        Objects.requireNonNull(money, "money is required");
        if (money.isZeroOrNegative()) {
            throw new DomainException("Base price must be greater than zero");
        }
        return money;
    }

    private static int requireEstimatedTime(int value) {
        if (value <= 0) {
            throw new DomainException("Estimated time must be greater than zero");
        }
        return value;
    }

    public void update(String newName, String newDescription, Money newBasePrice, int newEstimatedTimeInMinutes) {
        name = DomainValidation.requireText(newName, "Name is required", NAME_MAX_LENGTH);
        description = DomainValidation.requireText(newDescription, "Description is required", DESCRIPTION_MAX_LENGTH);
        basePrice = requirePositiveMoney(newBasePrice);
        estimatedTimeInMinutes = requireEstimatedTime(newEstimatedTimeInMinutes);
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

    public String name() {
        return name;
    }

    public String description() {
        return description;
    }

    public Money basePrice() {
        return basePrice;
    }

    public int estimatedTimeInMinutes() {
        return estimatedTimeInMinutes;
    }

    public boolean active() {
        return active;
    }
}
