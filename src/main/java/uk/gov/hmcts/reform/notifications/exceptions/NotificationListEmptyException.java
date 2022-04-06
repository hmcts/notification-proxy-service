package uk.gov.hmcts.reform.notifications.exceptions;

import java.io.Serializable;

public class NotificationListEmptyException extends RuntimeException implements Serializable {

    public static final long serialVersionUID = 413287434;

    public NotificationListEmptyException(String message) {
        super(message);
    }

}
