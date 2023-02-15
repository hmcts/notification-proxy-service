package uk.gov.hmcts.reform.notifications.service;

import org.springframework.util.MultiValueMap;
import uk.gov.hmcts.reform.notifications.dtos.request.DocPreviewRequest;
import uk.gov.hmcts.reform.notifications.dtos.request.RefundNotificationEmailRequest;
import uk.gov.hmcts.reform.notifications.dtos.request.RefundNotificationLetterRequest;
import uk.gov.hmcts.reform.notifications.dtos.response.NotificationResponseDto;
import uk.gov.hmcts.reform.notifications.dtos.response.NotificationTemplatePreviewResponse;
import uk.gov.hmcts.reform.notifications.dtos.response.PostCodeResponse;
import uk.gov.service.notify.SendEmailResponse;
import uk.gov.service.notify.SendLetterResponse;

public interface NotificationService {

    SendEmailResponse sendEmailNotification(RefundNotificationEmailRequest emailNotificationRequest, MultiValueMap<String, String> headers);

    SendLetterResponse sendLetterNotification(RefundNotificationLetterRequest letterNotificationRequest, MultiValueMap<String, String> headers);

    NotificationResponseDto getNotification(String reference);

    NotificationTemplatePreviewResponse previewNotification(DocPreviewRequest docPreviewRequest, MultiValueMap<String, String> headers);

    PostCodeResponse getAddress(String postCode);

    void deleteNotification(String reference);
}
