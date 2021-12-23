package uk.gov.hmcts.reform.notifications.service;

import uk.gov.hmcts.reform.notifications.dtos.request.EmailNotificationRequest;
import uk.gov.service.notify.NotificationClientException;
import uk.gov.service.notify.SendEmailResponse;

public interface NotificationService {

    SendEmailResponse sendEmailNotification(EmailNotificationRequest emailNotificationRequest, String templateId) throws NotificationClientException;

    void sendLetterNotification() throws NotificationClientException;
}
