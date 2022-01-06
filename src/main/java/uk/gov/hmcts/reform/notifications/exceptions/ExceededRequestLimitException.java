package uk.gov.hmcts.reform.notifications.exceptions;

public class ExceededRequestLimitException extends RuntimeException{
    public ExceededRequestLimitException (String message) {
        super(message);
    }
}
