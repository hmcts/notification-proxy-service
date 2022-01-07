package uk.gov.hmcts.reform.notifications.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class ExceededRequestLimitException extends RuntimeException{
    public ExceededRequestLimitException (String message) {
        super(message);
    }
}
