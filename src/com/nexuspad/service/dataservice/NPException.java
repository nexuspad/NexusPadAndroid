package com.nexuspad.service.dataservice;

public class NPException extends Exception {

    private ServiceError serviceError;

    /**
     * for serialization.
     */
    private static final long serialVersionUID = -3752066679502632246L;

    public NPException(ErrorCode code, String message) {
        super(message);
        serviceError = new ServiceError(code, message);
    }

    public NPException(ServiceError serviceError) {
        this.serviceError = serviceError;
    }

    public ServiceError getServiceError() {
        return serviceError;
    }

    public void setServiceError(ServiceError serviceError) {
        this.serviceError = serviceError;
    }
}
