package uk.gov.hmcts.reform.notifications.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.notifications.model.Notification;
import uk.gov.hmcts.reform.notifications.util.NotificationType;
import uk.gov.service.notify.SendEmailResponse;

@Component
public class EmailNotificationMapper {

    public Notification emailResponseMapper(SendEmailResponse sendEmailResponse) {
        return Notification.builder()
            .notificationType(NotificationType.EMAIL.getType())
            .reference(sendEmailResponse.getReference().get())
            .templateId(sendEmailResponse.getTemplateId().toString())
            .createdBy("System")
            .build();
    }

}
