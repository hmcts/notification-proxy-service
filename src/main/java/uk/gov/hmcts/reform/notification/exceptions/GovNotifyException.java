package uk.gov.hmcts.reform.notification.exceptions;

public class GovNotifyException extends RuntimeException {
    private static final long serialVersionUID = 42354L;
    public GovNotifyException (String message) {
        super(message);
    }
}
