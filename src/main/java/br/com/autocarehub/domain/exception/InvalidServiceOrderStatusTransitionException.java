package br.com.autocarehub.domain.exception;

import java.io.Serial;

public class InvalidServiceOrderStatusTransitionException extends DomainException {

    @Serial
    private static final long serialVersionUID = 1L;

    public InvalidServiceOrderStatusTransitionException(String message) {
        super(message);
    }
}
