package uk.gov.hmcts.reform.notification.exceptions;

public class GovNotifyUnmappedException extends GovNotifyException {
    private static final long serialVersionUID = 42347L;
        public GovNotifyUnmappedException (String message) {
            super(message);
    }

}
