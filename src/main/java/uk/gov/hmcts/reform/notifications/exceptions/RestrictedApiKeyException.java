package uk.gov.hmcts.reform.notifications.exceptions;

public class RestrictedApiKeyException extends RuntimeException{
    public RestrictedApiKeyException (String message) {
        super(message);
    }

}
