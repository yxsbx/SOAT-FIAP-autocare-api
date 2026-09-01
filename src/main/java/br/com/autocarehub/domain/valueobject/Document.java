package br.com.autocarehub.domain.valueobject;

import br.com.autocarehub.domain.enums.DocumentType;
import br.com.autocarehub.domain.exception.DomainException;
import java.util.Objects;
import java.util.regex.Pattern;

public record Document(DocumentType type, String value) {

    private static final int CPF_LENGTH = 11;
    private static final int CNPJ_LENGTH = 14;
    private static final int CPF_FIRST_DIGIT_INDEX = 9;
    private static final int CPF_SECOND_DIGIT_INDEX = 10;
    private static final int CNPJ_FIRST_DIGIT_INDEX = 12;
    private static final int CNPJ_SECOND_DIGIT_INDEX = 13;
    private static final int DIGIT_MODULUS = 11;
    private static final int DIGIT_REMAINDER_THRESHOLD = 2;
    private static final Pattern NON_DIGIT_PATTERN = Pattern.compile("\\D");
    private static final int[] CNPJ_FIRST_DIGIT_WEIGHTS = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
    private static final int[] CNPJ_SECOND_DIGIT_WEIGHTS = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

    public Document {
        Objects.requireNonNull(type, "type is required");
        value = normalize(value);
        if (!isValid(type, value)) {
            throw new DomainException("Invalid document");
        }
    }

    public static Document from(String value) {
        String normalized = normalize(value);
        if (normalized.length() == CPF_LENGTH) {
            return new Document(DocumentType.CPF, normalized);
        }
        if (normalized.length() == CNPJ_LENGTH) {
            return new Document(DocumentType.CNPJ, normalized);
        }
        throw new DomainException("Document must be CPF or CNPJ");
    }

    private static String normalize(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return NON_DIGIT_PATTERN.matcher(value).replaceAll("");
    }

    private static boolean isValid(DocumentType type, String value) {
        return switch (type) {
            case CPF -> isValidCpf(value);
            case CNPJ -> isValidCnpj(value);
        };
    }

    private static boolean isValidCpf(String value) {
        if (value.length() != CPF_LENGTH || hasSameDigits(value)) {
            return false;
        }
        int firstDigit = calculateCpfDigit(value, CPF_FIRST_DIGIT_INDEX);
        int secondDigit = calculateCpfDigit(value, CPF_SECOND_DIGIT_INDEX);
        return firstDigit == Character.getNumericValue(value.charAt(CPF_FIRST_DIGIT_INDEX))
                && secondDigit == Character.getNumericValue(value.charAt(CPF_SECOND_DIGIT_INDEX));
    }

    private static int calculateCpfDigit(String value, int length) {
        int sum = 0;
        for (int index = 0; index < length; index++) {
            sum += Character.getNumericValue(value.charAt(index)) * (length + 1 - index);
        }
        int remainder = sum % DIGIT_MODULUS;
        return remainder < DIGIT_REMAINDER_THRESHOLD ? 0 : DIGIT_MODULUS - remainder;
    }

    private static boolean isValidCnpj(String value) {
        if (value.length() != CNPJ_LENGTH || hasSameDigits(value)) {
            return false;
        }
        int firstDigit = calculateCnpjDigit(value, CNPJ_FIRST_DIGIT_INDEX, CNPJ_FIRST_DIGIT_WEIGHTS);
        int secondDigit = calculateCnpjDigit(value, CNPJ_SECOND_DIGIT_INDEX, CNPJ_SECOND_DIGIT_WEIGHTS);
        return firstDigit == Character.getNumericValue(value.charAt(CNPJ_FIRST_DIGIT_INDEX))
                && secondDigit == Character.getNumericValue(value.charAt(CNPJ_SECOND_DIGIT_INDEX));
    }

    private static int calculateCnpjDigit(String value, int length, int[] weights) {
        int sum = 0;
        for (int index = 0; index < length; index++) {
            sum += Character.getNumericValue(value.charAt(index)) * weights[index];
        }
        int remainder = sum % DIGIT_MODULUS;
        return remainder < DIGIT_REMAINDER_THRESHOLD ? 0 : DIGIT_MODULUS - remainder;
    }

    private static boolean hasSameDigits(String value) {
        char first = value.charAt(0);
        for (int index = 1; index < value.length(); index++) {
            if (value.charAt(index) != first) {
                return false;
            }
        }
        return true;
    }
}
