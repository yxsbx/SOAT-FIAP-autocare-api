package br.com.autocarehub.domain.model;

import br.com.autocarehub.domain.enums.StockMovementType;
import br.com.autocarehub.domain.exception.DomainException;
import br.com.autocarehub.domain.valueobject.Money;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public record StockMovement(
        UUID partId,
        StockMovementType type,
        int quantity,
        Money unitCost,
        Money unitPrice,
        String reason,
        LocalDateTime occurredAt) {

    public StockMovement {
        Objects.requireNonNull(partId, "partId is required");
        Objects.requireNonNull(type, "type is required");
        if (quantity <= 0) {
            throw new DomainException("Quantity must be greater than zero");
        }
        reason = reason.trim();
    }
}
