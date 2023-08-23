package uk.gov.hmcts.reform.notification.service;

import org.springframework.util.MultiValueMap;
import uk.gov.hmcts.reform.notification.dtos.request.DocPreviewRequest;
import uk.gov.hmcts.reform.notification.dtos.request.NotificationRequest;
import uk.gov.hmcts.reform.notification.dtos.request.RefundNotificationLetterRequest;
import uk.gov.hmcts.reform.notification.dtos.request.SchedulerRequest;
import uk.gov.hmcts.reform.notification.dtos.response.NotificationResponseDto;
import uk.gov.hmcts.reform.notification.dtos.response.NotificationTemplatePreviewResponse;
import uk.gov.hmcts.reform.notification.dtos.response.PostCodeResponse;
import uk.gov.service.notify.SendEmailResponse;
import uk.gov.service.notify.SendLetterResponse;

public interface NotificationService {

    SendEmailResponse saveNotificationRequest(NotificationRequest emailNotificationRequest, MultiValueMap<String, String> headers);

    SendLetterResponse sendLetterNotification(RefundNotificationLetterRequest letterNotificationRequest, MultiValueMap<String, String> headers);

    NotificationResponseDto getNotification(String reference);

    NotificationTemplatePreviewResponse previewNotification(DocPreviewRequest docPreviewRequest, MultiValueMap<String, String> headers);

    PostCodeResponse getAddress(String postCode);

    void deleteNotification(String reference);

    void sendEmailNotification(SchedulerRequest schedulerRequest);
}
