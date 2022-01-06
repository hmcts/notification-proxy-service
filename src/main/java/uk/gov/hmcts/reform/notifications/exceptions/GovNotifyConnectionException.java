package uk.gov.hmcts.reform.notifications.exceptions;

public class GovNotifyConnectionException extends RuntimeException{
    public GovNotifyConnectionException (String message) {
        super(message);
    }
}
