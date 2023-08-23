package uk.gov.hmcts.reform.notification.exceptions;

public class InvalidApiKeyException extends GovNotifyException{
    private static final long serialVersionUID = 42349L;
    public InvalidApiKeyException (String message) {
        super(message);
    }
}
