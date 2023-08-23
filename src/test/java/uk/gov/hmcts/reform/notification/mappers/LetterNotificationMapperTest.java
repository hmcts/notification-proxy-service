package uk.gov.hmcts.reform.notification.mappers;

import java.util.Arrays;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.notification.dtos.enums.NotificationType;
import uk.gov.hmcts.reform.notification.dtos.request.Personalisation;
import uk.gov.hmcts.reform.notification.dtos.request.RecipientPostalAddress;
import uk.gov.hmcts.reform.notification.dtos.request.RefundNotificationLetterRequest;
import uk.gov.hmcts.reform.notification.dtos.response.IdamUserIdResponse;
import uk.gov.hmcts.reform.notification.mapper.LetterNotificationMapper;
import uk.gov.hmcts.reform.notification.model.Notification;
import uk.gov.service.notify.SendLetterResponse;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.MOCK;

@ActiveProfiles({"local", "test"})
@SpringBootTest(webEnvironment = MOCK)
@SuppressWarnings("PMD")
public class LetterNotificationMapperTest {

    @Autowired
    private LetterNotificationMapper letterNotificationMapper;

    @Mock
    private  SendLetterResponse sendLetterResponse;

    @Test
    public void testLetterNotificationMapper() {
        when(sendLetterResponse.getTemplateId()).thenReturn(UUID.fromString("8833960c-4ffa-42db-806c-451a68c56e98"));
        when(sendLetterResponse.getReference()).thenReturn(java.util.Optional.of("RF-123"));

        Notification  notification= letterNotificationMapper.letterResponseMapper(sendLetterResponse,getRefundNotification(),IDAM_USER_ID_RESPONSE);
        assertEquals("LETTER", notification.getNotificationType());
        assertEquals("RF-123", notification.getReference());
        assertEquals("8833960c-4ffa-42db-806c-451a68c56e98", notification.getTemplateId());
        assertEquals("e30ccf3a-8457-4e45-b251-74a346e7ec88", notification.getCreatedBy());
        assertEquals("11 King street", notification.getContactDetails().getAddressLine());


    }

    private RefundNotificationLetterRequest getRefundNotification(){
        return RefundNotificationLetterRequest.refundNotificationLetterRequestWith()
            .notificationType(NotificationType.LETTER)
            .recipientPostalAddress(getRecipientPostalAddress())
            .reference("REF-123")
            .templateId("7ed517e8-b34d-4aa6-8822-afb578a0a69d")
            .personalisation(getPersonalisation())
            .build();
    }

    private RecipientPostalAddress getRecipientPostalAddress(){
        return RecipientPostalAddress.recipientPostalAddressWith()
            .addressLine("11 King street")
            .postalCode("e146kk")
            .city("london")
            .country("UK")
            .county("london")
            .build();

    }

    private Personalisation getPersonalisation(){

        return Personalisation.personalisationRequestWith()
            .ccdCaseNumber("1111222233334444")
            .refundReference("REF-123")
            .build();
    }

    private static final IdamUserIdResponse IDAM_USER_ID_RESPONSE =
        IdamUserIdResponse.idamUserIdResponseWith().uid("e30ccf3a-8457-4e45-b251-74a346e7ec88").givenName("XX").familyName("YY")
            .name("XX YY")
            .roles(Arrays.asList("payments-refund-approver", "payments-refund")).sub("ZZ")
            .build();
}
