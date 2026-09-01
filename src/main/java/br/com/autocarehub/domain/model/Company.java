package br.com.autocarehub.domain.model;

import br.com.autocarehub.domain.exception.DomainException;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public record Company(UUID id, String name, String type, boolean active, LocalDateTime createdAt) {

    public Company {
        Objects.requireNonNull(id, "id is required");
        name = requireText(name, "Company name is required");
        type = normalizeType(type);
        Objects.requireNonNull(createdAt, "createdAt is required");
    }

    public static Company create(String name, String type) {
        return new Company(UUID.randomUUID(), name, type, true, LocalDateTime.now());
    }

    private static String requireText(String value, String message) {
        if (value.isBlank()) {
            throw new DomainException(message);
        }
        return value.trim();
    }

    private static String normalizeType(String value) {
        String type = requireText(value, "Company type is required").toUpperCase();
        if (!type.equals("PLATFORM") && !type.equals("WORKSHOP") && !type.equals("PARTS_STORE")) {
            throw new DomainException("Invalid company type");
        }
        return type;
    }
}
