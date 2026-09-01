package br.com.autocarehub.application.exception;

import java.io.Serial;

public class ResourceNotFoundException extends ApplicationException {

    @Serial
    private static final long serialVersionUID = 1L;

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
