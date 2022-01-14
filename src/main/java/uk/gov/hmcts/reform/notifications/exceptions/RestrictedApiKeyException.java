package uk.gov.hmcts.reform.notifications.exceptions;

public class RestrictedApiKeyException extends GovNotifyException{
    private static final long serialVersionUID = 42351L;
    public RestrictedApiKeyException (String message) {
        super(message);
    }

}
