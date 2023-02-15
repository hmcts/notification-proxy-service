package uk.gov.hmcts.reform.notifications.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.notifications.dtos.response.ContactDetailsDto;
import uk.gov.hmcts.reform.notifications.dtos.response.FromTemplateContact;
import uk.gov.hmcts.reform.notifications.dtos.response.MailAddress;
import uk.gov.hmcts.reform.notifications.dtos.response.NotificationDto;
import uk.gov.hmcts.reform.notifications.dtos.response.NotificationTemplatePreviewResponse;
import uk.gov.hmcts.reform.notifications.dtos.response.RecipientContact;
import uk.gov.hmcts.reform.notifications.model.Notification;

@Component
public class NotificationResponseMapper {

    private static final String EMAIL = "EMAIL";
    private static final String LETTER = "LETTER";

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
            .sentNotification(toSentNotification(notification))
            .build();
    }

    private NotificationTemplatePreviewResponse toSentNotification(Notification notification){

          return NotificationTemplatePreviewResponse.buildNotificationTemplatePreviewWith()
              .templateId(notification.getTemplatePreview().getId().toString())
              .templateType(notification.getTemplatePreview().getTemplateType())
              .subject(notification.getTemplatePreview().getSubject())
              .html(notification.getTemplatePreview().getHtml())
              .from(toFromMapper(notification))
              .recipientContact(toContactMapper(notification))
              .body(notification.getTemplatePreview().getBody())
              .build();
    }

    private RecipientContact toContactMapper(Notification notification) {

        return RecipientContact.buildRecipientContactWith()
            .recipientEmailAddress(toEmailMapper(notification))
            .recipientMailAddress(toMailMapper(notification))
            .build();
    }

    private String toEmailMapper(Notification notification) {

        String email = null;
        if(EMAIL.equalsIgnoreCase(notification.getNotificationType())) {

            email = notification.getContactDetails().getEmail();

        }

        return email;
    }

    private MailAddress toMailMapper(Notification notification) {

        MailAddress recipientMailAddress = null;
        if(LETTER.equalsIgnoreCase(notification.getNotificationType())) {

            recipientMailAddress = MailAddress.buildRecipientMailAddressWith()
                .addressLine(notification.getContactDetails().getAddressLine())
                .city(notification.getContactDetails().getCity())
                .county(notification.getContactDetails().getCounty())
                .country(notification.getContactDetails().getCountry())
                .postalCode(notification.getContactDetails().getPostcode())
                .build();
        }

        return recipientMailAddress;
    }


    public FromTemplateContact toFromMapper(Notification notification) {

        return FromTemplateContact.buildFromTemplateContactWith()
            .fromEmailAddress(toFromEmailMapper(notification))
            .fromMailAddress(toFromMailMapper(notification))
            .build();
    }

    private String toFromEmailMapper(Notification notification) {

        String email = null;
        if(EMAIL.equalsIgnoreCase(notification.getNotificationType())) {
            email = notification.getTemplatePreview().getFrom().getFromEmailAddress();
        }
        return email;
    }

    private MailAddress toFromMailMapper(Notification notification) {

        MailAddress fromMailAddress = null;
        if(null !=notification.getTemplatePreview().getFrom().getFromMailAddress() && LETTER.equalsIgnoreCase(notification.getNotificationType())) {

            fromMailAddress = MailAddress.buildRecipientMailAddressWith()
                .addressLine(notification.getTemplatePreview().getFrom().getFromMailAddress().getAddressLine())
                .city(notification.getTemplatePreview().getFrom().getFromMailAddress().getCity())
                .county(notification.getTemplatePreview().getFrom().getFromMailAddress().getCounty())
                .country(notification.getTemplatePreview().getFrom().getFromMailAddress().getCountry())
                .postalCode(notification.getTemplatePreview().getFrom().getFromMailAddress().getPostalCode())
                .build();
        }
        return fromMailAddress;
    }
}
