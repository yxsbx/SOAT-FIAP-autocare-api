package br.com.autocarehub.domain.valueobject;

import br.com.autocarehub.domain.exception.DomainException;
import java.util.Locale;
import java.util.regex.Pattern;

public record Plate(String value) {

    private static final Pattern NON_ALPHANUMERIC_PATTERN = Pattern.compile("[^A-Za-z0-9]");
    private static final Pattern OLD_BR_PLATE_PATTERN = Pattern.compile("^[A-Z]{3}[0-9]{4}$");
    private static final Pattern MERCOSUR_PLATE_PATTERN = Pattern.compile("^[A-Z]{3}[0-9][A-Z][0-9]{2}$");

    public Plate {
        if (value == null || value.isBlank()) {
            throw new DomainException("Plate is required");
        }
        value = NON_ALPHANUMERIC_PATTERN.matcher(value).replaceAll("").toUpperCase(Locale.ROOT);
        if (!OLD_BR_PLATE_PATTERN.matcher(value).matches()
                && !MERCOSUR_PLATE_PATTERN.matcher(value).matches()) {
            throw new DomainException("Invalid plate");
        }
    }
}
