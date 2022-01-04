package uk.gov.hmcts.reform.notifications.service;

import uk.gov.hmcts.reform.notifications.dtos.request.EmailNotificationRequest;
import uk.gov.hmcts.reform.notifications.dtos.request.RefundNotificationEmailRequest;
import uk.gov.hmcts.reform.notifications.dtos.request.RefundNotificationLetterRequest;
import uk.gov.service.notify.NotificationClientException;
import uk.gov.service.notify.SendEmailResponse;
import uk.gov.service.notify.SendLetterResponse;

public interface NotificationService {

    SendEmailResponse sendEmailNotification(RefundNotificationEmailRequest emailNotificationRequest) throws NotificationClientException;

    SendLetterResponse sendLetterNotification(RefundNotificationLetterRequest letterNotificationRequest) throws NotificationClientException;
}
