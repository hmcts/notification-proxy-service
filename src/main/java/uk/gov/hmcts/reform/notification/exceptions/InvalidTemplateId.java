package uk.gov.hmcts.reform.notification.exceptions;

public class InvalidTemplateId extends GovNotifyException {
    private static final long serialVersionUID = 42350L;
    public InvalidTemplateId (String message) {
        super(message);
    }
}
