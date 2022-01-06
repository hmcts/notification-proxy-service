package uk.gov.hmcts.reform.notifications.exceptions;

public class InvalidApiKeyException extends RuntimeException{
    public InvalidApiKeyException (String message) {
        super(message);
    }
}
