package uk.gov.hmcts.reform.notifications.exceptions;

public class InvalidApiKeyException extends GovNotifyException{
    private static final long serialVersionUID = 42349L;
    public InvalidApiKeyException (String message) {
        super(message);
    }
}
