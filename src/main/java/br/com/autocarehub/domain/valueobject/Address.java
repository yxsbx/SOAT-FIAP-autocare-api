package br.com.autocarehub.domain.valueobject;

import br.com.autocarehub.domain.exception.DomainException;
import br.com.autocarehub.domain.service.DomainValidation;
import java.util.regex.Pattern;
import org.jspecify.annotations.Nullable;

public record Address(
        String street,
        String number,
        @Nullable String complement,
        String neighborhood,
        String city,
        String state,
        String zipCode) {

    private static final int STREET_MAX_LENGTH = 120;
    private static final int NUMBER_MAX_LENGTH = 20;
    private static final int NEIGHBORHOOD_MAX_LENGTH = 80;
    private static final int CITY_MAX_LENGTH = 80;
    private static final int ZIP_CODE_MAX_LENGTH = 9;
    private static final int ZIP_CODE_DIGITS = 8;
    private static final Pattern NON_DIGIT_PATTERN = Pattern.compile("\\D");

    public Address {
        street = DomainValidation.requireText(street, "Street is required", STREET_MAX_LENGTH);
        number = DomainValidation.requireText(number, "Number is required", NUMBER_MAX_LENGTH);
        complement = DomainValidation.optionalText(complement);
        neighborhood = DomainValidation.requireText(neighborhood, "Neighborhood is required", NEIGHBORHOOD_MAX_LENGTH);
        city = DomainValidation.requireText(city, "City is required", CITY_MAX_LENGTH);
        state = DomainValidation.requireState(state);
        zipCode = NON_DIGIT_PATTERN
                .matcher(DomainValidation.requireText(zipCode, "Zip code is required", ZIP_CODE_MAX_LENGTH))
                .replaceAll("");
        if (zipCode.length() != ZIP_CODE_DIGITS) {
            throw new DomainException("Zip code must have eight digits");
        }
    }
}
