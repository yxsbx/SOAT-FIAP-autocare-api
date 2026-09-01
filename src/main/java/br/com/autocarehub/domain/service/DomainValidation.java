package br.com.autocarehub.domain.service;

import br.com.autocarehub.domain.exception.DomainException;
import java.time.Year;
import java.util.Locale;
import java.util.regex.Pattern;
import org.jspecify.annotations.Nullable;

public final class DomainValidation {

    private static final int DEFAULT_OPTIONAL_TEXT_MAX_LENGTH = 80;
    private static final int EMAIL_MAX_LENGTH = 120;
    private static final int PHONE_MAX_LENGTH = 20;
    private static final int STATE_MAX_LENGTH = 2;
    private static final int MIN_VEHICLE_YEAR = 1900;

    private static final Pattern NON_DIGIT_PATTERN = Pattern.compile("\\D");
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", Pattern.CASE_INSENSITIVE);
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\d{10,11}$");
    private static final Pattern STATE_PATTERN = Pattern.compile("^[A-Z]{2}$");

    private DomainValidation() {}

    public static String requireText(String value, String message, int maxLength) {
        if (value.isBlank()) {
            throw new DomainException(message);
        }
        String normalized = value.trim();
        if (normalized.length() > maxLength) {
            throw new DomainException("Text exceeds maximum length");
        }
        return normalized;
    }

    public static @Nullable String optionalText(@Nullable String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim();
        if (normalized.length() > DEFAULT_OPTIONAL_TEXT_MAX_LENGTH) {
            throw new DomainException("Text exceeds maximum length");
        }
        return normalized;
    }

    public static String requireEmail(String value) {
        String email = requireText(value, "Email is required", EMAIL_MAX_LENGTH).toLowerCase(Locale.ROOT);
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new DomainException("Invalid email");
        }
        return email;
    }

    public static String requirePhone(String value) {
        String phone = NON_DIGIT_PATTERN
                .matcher(requireText(value, "Phone is required", PHONE_MAX_LENGTH))
                .replaceAll("");
        if (!PHONE_PATTERN.matcher(phone).matches()) {
            throw new DomainException("Invalid phone");
        }
        return phone;
    }

    public static String requireState(String value) {
        String state = requireText(value, "State is required", STATE_MAX_LENGTH).toUpperCase(Locale.ROOT);
        if (!STATE_PATTERN.matcher(state).matches()) {
            throw new DomainException("State must have two characters");
        }
        return state;
    }

    public static int requireVehicleYear(int value) {
        int nextModelYear = Year.now().getValue() + 1;
        if (value < MIN_VEHICLE_YEAR || value > nextModelYear) {
            throw new DomainException("Invalid year");
        }
        return value;
    }
}
