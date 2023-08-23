package uk.gov.hmcts.reform.notification.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.notification.dtos.request.RefundNotificationEmailRequest;
import uk.gov.hmcts.reform.notification.dtos.response.IdamUserIdResponse;
import uk.gov.hmcts.reform.notification.model.ContactDetails;
import uk.gov.hmcts.reform.notification.model.Notification;


@Component
public class EmailNotificationMapper {

    public Notification emailResponseMapper( RefundNotificationEmailRequest emailNotificationRequest, IdamUserIdResponse uid) {
        ContactDetails contactDetailsList = ContactDetails.contactDetailsWith()
                                   .email(emailNotificationRequest.getRecipientEmailAddress())
                                   .createdBy(uid.getUid())
                                   .build();
        Notification notification = Notification.builder()
            .notificationType(emailNotificationRequest.getNotificationType().toString())
            .reference(emailNotificationRequest.getReference())
            .templateId(emailNotificationRequest.getTemplateId())
            .createdBy(uid.getUid())
            .contactDetails(contactDetailsList)
            .build();
        contactDetailsList.setNotification(notification);
        notification.setContactDetails(contactDetailsList);
        return notification;
    }

}
