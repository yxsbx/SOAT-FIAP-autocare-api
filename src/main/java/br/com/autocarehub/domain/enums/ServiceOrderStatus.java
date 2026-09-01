package br.com.autocarehub.domain.enums;

import br.com.autocarehub.domain.exception.DomainException;

public enum ServiceOrderStatus {
    RECEBIDA("RECEIVED"),
    EM_DIAGNOSTICO("IN_DIAGNOSIS"),
    AGUARDANDO_APROVACAO("WAITING_APPROVAL"),
    EM_EXECUCAO("IN_PROGRESS"),
    FINALIZADA("FINISHED"),
    ENTREGUE("DELIVERED");

    private final String externalCode;

    ServiceOrderStatus(String externalCode) {
        this.externalCode = externalCode;
    }

    public static ServiceOrderStatus fromExternalCode(String value) {
        for (ServiceOrderStatus status : values()) {
            if (status.name().equals(value) || status.externalCode.equals(value)) {
                return status;
            }
        }
        throw new DomainException("Invalid service order status");
    }

    public String externalCode() {
        return externalCode;
    }
}
