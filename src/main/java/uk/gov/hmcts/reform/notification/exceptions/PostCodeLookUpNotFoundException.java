package uk.gov.hmcts.reform.notification.exceptions;

import java.io.Serializable;

public class PostCodeLookUpNotFoundException extends RuntimeException implements Serializable {

    public static final long serialVersionUID = 43287434;

    public PostCodeLookUpNotFoundException(String message) {
        super(message);
    }

}
