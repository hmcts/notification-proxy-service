package uk.gov.hmcts.reform.notifications.exceptions;

public class InvalidTemplateId extends RuntimeException {
    public InvalidTemplateId (String message) {
        super(message);
    }
}
