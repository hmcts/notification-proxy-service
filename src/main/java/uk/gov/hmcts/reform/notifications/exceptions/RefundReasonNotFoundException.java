package uk.gov.hmcts.reform.notifications.exceptions;

public class RefundReasonNotFoundException extends RuntimeException {
    public static final long serialVersionUID = 413287439;

    public RefundReasonNotFoundException(String message) {
        super(message);
    }

}
