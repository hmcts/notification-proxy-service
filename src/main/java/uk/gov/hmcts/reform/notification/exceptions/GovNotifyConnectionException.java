package uk.gov.hmcts.reform.notification.exceptions;

public class GovNotifyConnectionException extends GovNotifyException{
    private static final long serialVersionUID = 42346L;
    public GovNotifyConnectionException (String message) {
        super(message);
    }
}
