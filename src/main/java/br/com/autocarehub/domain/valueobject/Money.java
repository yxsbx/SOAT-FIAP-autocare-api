package br.com.autocarehub.domain.valueobject;

import br.com.autocarehub.domain.exception.DomainException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public record Money(BigDecimal value) implements Comparable<Money> {

    public Money {
        Objects.requireNonNull(value, "value is required");
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new DomainException("Money cannot be negative");
        }
        value = value.setScale(2, RoundingMode.HALF_UP);
    }

    public static Money zero() {
        return new Money(BigDecimal.ZERO);
    }

    public static Money of(BigDecimal value) {
        return new Money(value);
    }

    public static Money of(String value) {
        return new Money(new BigDecimal(value));
    }

    public Money add(Money other) {
        return new Money(value.add(other.value));
    }

    public Money multiply(int quantity) {
        if (quantity < 0) {
            throw new DomainException("Quantity cannot be negative");
        }
        return new Money(value.multiply(BigDecimal.valueOf(quantity)));
    }

    public boolean isZero() {
        return value.compareTo(BigDecimal.ZERO) == 0;
    }

    public boolean isZeroOrNegative() {
        return value.signum() <= 0;
    }

    @Override
    public int compareTo(Money other) {
        return value.compareTo(other.value);
    }
}
