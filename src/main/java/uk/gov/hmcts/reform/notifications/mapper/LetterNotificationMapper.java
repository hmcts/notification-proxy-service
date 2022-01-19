package uk.gov.hmcts.reform.notifications.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.notifications.dtos.request.RefundNotificationLetterRequest;
import uk.gov.hmcts.reform.notifications.dtos.response.IdamUserIdResponse;
import uk.gov.hmcts.reform.notifications.model.ContactDetails;
import uk.gov.hmcts.reform.notifications.model.Notification;
import uk.gov.service.notify.SendLetterResponse;


@Component
public class LetterNotificationMapper {

    public Notification letterResponseMapper(SendLetterResponse sendLetterResponse, RefundNotificationLetterRequest letterNotificationRequest,
                                              IdamUserIdResponse uid) {
        ContactDetails contactDetailsList = ContactDetails.contactDetailsWith()
                                   .addressLine(letterNotificationRequest.getRecipientPostalAddress().getAddressLine())
                                   .postcode(letterNotificationRequest.getRecipientPostalAddress().getPostalCode())
                                   .county(letterNotificationRequest.getRecipientPostalAddress().getCounty())
                                   .city(letterNotificationRequest.getRecipientPostalAddress().getCity())
                                   .country(letterNotificationRequest.getRecipientPostalAddress().getCountry())
                                   .createdBy(uid.getUid())
                                   .build();
        Notification notification =  Notification.builder()
            .notificationType(letterNotificationRequest.getNotificationType().toString())
            .reference(sendLetterResponse.getReference().get())
            .templateId(sendLetterResponse.getTemplateId().toString())
            .createdBy(uid.getUid())
            .contactDetails(contactDetailsList)
            .build();

        contactDetailsList.setNotification(notification);
        notification.setContactDetails(contactDetailsList);
        return notification;
    }
}
