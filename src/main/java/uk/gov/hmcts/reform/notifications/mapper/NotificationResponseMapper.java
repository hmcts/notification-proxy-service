package uk.gov.hmcts.reform.notifications.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.notifications.dtos.response.ContactDetailsDto;
import uk.gov.hmcts.reform.notifications.dtos.response.NotificationDto;
import uk.gov.hmcts.reform.notifications.model.Notification;

@Component
public class NotificationResponseMapper {

    public NotificationDto notificationResponse(Notification notification){

        return NotificationDto.buildNotificationWith()
            .notificationType(notification.getNotificationType())
            .dateCreated(notification.getDateCreated())
            .dateUpdated(notification.getDateUpdated())
            .reference(notification.getReference())
            .contactDetails(
                ContactDetailsDto.buildContactDetailsWith()
                    .addressLine(notification.getContactDetails().getAddressLine())
                    .city(notification.getContactDetails().getCity())
                    .country(notification.getContactDetails().getCountry())
                    .email(notification.getContactDetails().getEmail())
                    .dateCreated(notification.getContactDetails().getDateCreated())
                    .postalCode(notification.getContactDetails().getPostcode())
                    .county(notification.getContactDetails().getCounty())
                    .dateUpdated(notification.getContactDetails().getDateUpdated())
                    .build()

            )
            .build();
    }

}
