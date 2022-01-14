package uk.gov.hmcts.reform.notifications.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.notifications.dtos.request.RefundNotificationEmailRequest;
import uk.gov.hmcts.reform.notifications.model.ContactDetails;
import uk.gov.hmcts.reform.notifications.model.Notification;


@Component
public class EmailNotificationMapper {

    public Notification emailResponseMapper( RefundNotificationEmailRequest emailNotificationRequest) {
        ContactDetails contactDetailsList = ContactDetails.contactDetailsWith()
                                   .email(emailNotificationRequest.getRecipientEmailAddress())
                                   .createdBy("System")
                                   .build();
        Notification notification = Notification.builder()
            .notificationType(emailNotificationRequest.getNotificationType().toString())
            .reference(emailNotificationRequest.getReference())
            .templateId(emailNotificationRequest.getTemplateId())
            .createdBy("System")
            .contactDetails(contactDetailsList)
            .build();
        contactDetailsList.setNotification(notification);
        notification.setContactDetails(contactDetailsList);
        return notification;
    }

}
