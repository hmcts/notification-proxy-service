package uk.gov.hmcts.reform.notifications.util;

import uk.gov.service.notify.NotificationClientException;

public class GovNotifyExceptionWrapper {

    public Exception mapGovNotifyException(NotificationClientException e){
        return e;
    }
}
