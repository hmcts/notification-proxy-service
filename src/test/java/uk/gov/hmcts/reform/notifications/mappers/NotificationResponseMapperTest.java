package uk.gov.hmcts.reform.notifications.mappers;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.notifications.dtos.response.ContactDetailsDto;
import uk.gov.hmcts.reform.notifications.dtos.response.FromTemplateContact;
import uk.gov.hmcts.reform.notifications.dtos.response.MailAddress;
import uk.gov.hmcts.reform.notifications.dtos.response.NotificationTemplatePreviewResponse;
import uk.gov.hmcts.reform.notifications.mapper.NotificationResponseMapper;
import uk.gov.hmcts.reform.notifications.dtos.response.NotificationDto;
import uk.gov.hmcts.reform.notifications.model.ContactDetails;
import uk.gov.hmcts.reform.notifications.model.Notification;
import uk.gov.hmcts.reform.notifications.model.TemplatePreviewDto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.MOCK;

@ActiveProfiles({"local", "test"})
@SpringBootTest(webEnvironment = MOCK)
@SuppressWarnings("PMD")
public class NotificationResponseMapperTest {

    @Autowired
    private NotificationResponseMapper notificationResponseMapper;

    DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    String dateString = "2021-02-11";
    Date dateObject = sdf.parse(dateString);

    public NotificationResponseMapperTest() throws ParseException {
    }

    private NotificationDto getNotificationdeatils() {
        return NotificationDto.buildNotificationWith()
            .notificationType("LETTER")
            .dateCreated(dateObject)
            .dateUpdated(dateObject)
            .reference("RF123")
            .contactDetails(
                ContactDetailsDto.buildContactDetailsWith()
                    .addressLine("11 King street")
                    .city("london")
                    .country("UK")
                    .email("null")
                    .dateCreated(dateObject)
                    .dateUpdated(dateObject)
                    .postalCode("e146kk")
                    .county("london")
                    .build()

            )
            .sentNotification(NotificationTemplatePreviewResponse.buildNotificationTemplatePreviewWith()
                                .templateId(getUUID().toString())
                                 .html("test")
                                 .body("test")
                                 .subject("testSubject")
                                 .templateType("email")
                                 .build())
            .build();
    }

    private  ContactDetails getContact() {

        return ContactDetails.contactDetailsWith()
            .id(1)
            .addressLine("11 King street")
            .city("london")
            .country("UK")
            .email("null")
            .dateCreated(dateObject)
            .postcode("e146kk")
            .county("london")
            .dateUpdated(dateObject)
            .createdBy("e30ccf3a-8457-4e45-b251-74a346e7ec88")
            .build();
    }

    private Notification refundListSupplierBasedOnRefundReference() {
        return Notification.builder()
            // public static final Supplier<Notification> refundListSupplierBasedOnRefundReference = () -> Notification.builder()
            .id(1)
            .notificationType("LETTER")
            .reference("RF123")
            .templateId("8833960c-4ffa-42db-806c-451a68c56e98")
            .createdBy("e30ccf3a-8457-4e45-b251-74a346e7ec88")
            .dateUpdated(dateObject)
            .dateCreated(dateObject)
            .contactDetails(getContact())
            .templatePreview(TemplatePreviewDto.templatePreviewDtoWith()
                                 .id(getUUID())
                                  .html("test")
                                  .body("test")
                                  .subject("testSubject")
                                  .templateType("email")
                                 .from(FromTemplateContact
                                           .buildFromTemplateContactWith()
                                           .fromMailAddress(
                                               MailAddress
                                                   .buildRecipientMailAddressWith()
                                                   .addressLine("6 Test")
                                                   .city("city")
                                                   .country("country")
                                                   .county("county")
                                                   .postalCode("HA3 5TT")
                                                   .build())
                                           .build())
                                 .build())

            .build();


    }

    @Test
    public void testGetNotificationResponseMapper() {

        NotificationDto  notificationDto= notificationResponseMapper
            .notificationResponse(refundListSupplierBasedOnRefundReference());

        assertThat(notificationDto.getNotificationType()).isEqualTo(getNotificationdeatils().getNotificationType());
        assertThat(notificationDto.getSentNotification().getSubject()).isEqualTo(getNotificationdeatils().getSentNotification().getSubject());
    }

    private UUID getUUID(){

        String superSecretId = "f000aa01-0451-4000-b000-000000000000";
       return UUID.fromString(superSecretId);
    }
}
