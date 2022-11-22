package uk.gov.hmcts.reform.notifications.exceptions;

public class DocPreviewBadRequestException extends RuntimeException {

    public static final long serialVersionUID = 413287436;

    public DocPreviewBadRequestException(String message) {
        super(message);
    }
}
