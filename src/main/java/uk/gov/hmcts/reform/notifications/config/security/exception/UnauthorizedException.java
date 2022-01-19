package uk.gov.hmcts.reform.notifications.config.security.exception;

public class UnauthorizedException extends RuntimeException {

    public static final long serialVersionUID = 4328743;

    public UnauthorizedException(String message) {
        super(message);
    }

    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }

}
