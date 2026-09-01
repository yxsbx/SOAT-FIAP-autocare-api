package br.com.autocarehub.domain.model;

import br.com.autocarehub.domain.exception.DomainException;
import br.com.autocarehub.domain.valueobject.Money;
import java.util.Objects;
import java.util.UUID;

public record BudgetItem(UUID referenceId, String description, int quantity, Money unitPrice, Money totalPrice) {

    public BudgetItem(UUID referenceId, String description, int quantity, Money unitPrice) {
        this(referenceId, description, quantity, unitPrice, unitPrice.multiply(quantity));
    }

    public BudgetItem {
        Objects.requireNonNull(referenceId, "referenceId is required");
        description = requireText(description);
        if (quantity <= 0) {
            throw new DomainException("Quantity must be greater than zero");
        }
        Objects.requireNonNull(unitPrice, "unitPrice is required");
        Objects.requireNonNull(totalPrice, "totalPrice is required");
    }

    private static String requireText(String value) {
        if (value.isBlank()) {
            throw new DomainException("Budget item description is required");
        }
        return value.trim();
    }
}
