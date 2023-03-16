package uk.gov.hmcts.reform.notifications.exceptions;

import java.io.Serializable;

public class PostCodeLookUpException extends RuntimeException implements Serializable {
    private static final long serialVersionUID = 29923904L;

    public PostCodeLookUpException(String errorMessage) {
        super(errorMessage);
    }

    public PostCodeLookUpException(String message, Throwable cause) {
        super(message, cause);
    }
}
