package uk.gov.hmcts.reform.notifications.exceptions;

public class PaymentServerException extends RuntimeException {

    public static final long serialVersionUID = 413287436;

    public PaymentServerException(String message, Throwable cause) {
        super(message, cause);
    }
}


