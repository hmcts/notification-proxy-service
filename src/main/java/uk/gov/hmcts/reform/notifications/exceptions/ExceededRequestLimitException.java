package uk.gov.hmcts.reform.notifications.exceptions;

public class ExceededRequestLimitException extends GovNotifyException{
    private static final long serialVersionUID = 42345L;
    public ExceededRequestLimitException (String message) {
        super(message);
    }
}
