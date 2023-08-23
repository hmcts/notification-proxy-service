package uk.gov.hmcts.reform.notification.exceptions;

import java.io.Serializable;

public class GatewayTimeoutException extends RuntimeException implements Serializable {

    public static final long serialVersionUID = 43287432;

    public GatewayTimeoutException(String message) {
        super(message);
    }
}
