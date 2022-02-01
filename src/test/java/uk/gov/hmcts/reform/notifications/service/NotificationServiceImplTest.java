package uk.gov.hmcts.reform.notifications.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.MOCK;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.hmcts.reform.notifications.config.security.idam.IdamServiceImpl;
import uk.gov.hmcts.reform.notifications.dtos.enums.NotificationType;
import uk.gov.hmcts.reform.notifications.dtos.request.Personalisation;
import uk.gov.hmcts.reform.notifications.dtos.request.RecipientPostalAddress;
import uk.gov.hmcts.reform.notifications.dtos.request.RefundNotificationEmailRequest;
import uk.gov.hmcts.reform.notifications.dtos.request.RefundNotificationLetterRequest;
import uk.gov.hmcts.reform.notifications.dtos.response.IdamUserIdResponse;
import uk.gov.hmcts.reform.notifications.dtos.response.NotificationResponseDto;
import uk.gov.hmcts.reform.notifications.exceptions.InvalidAddressException;
import uk.gov.hmcts.reform.notifications.exceptions.InvalidApiKeyException;
import uk.gov.hmcts.reform.notifications.exceptions.InvalidTemplateId;
import uk.gov.hmcts.reform.notifications.exceptions.NotificationListEmptyException;
import uk.gov.hmcts.reform.notifications.mapper.EmailNotificationMapper;
import uk.gov.hmcts.reform.notifications.mapper.LetterNotificationMapper;
import uk.gov.hmcts.reform.notifications.mapper.NotificationResponseMapper;
import uk.gov.hmcts.reform.notifications.model.ContactDetails;
import uk.gov.hmcts.reform.notifications.model.Notification;
import uk.gov.hmcts.reform.notifications.repository.NotificationRepository;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import uk.gov.service.notify.NotificationClientApi;
import uk.gov.service.notify.NotificationClientException;
import uk.gov.service.notify.SendEmailResponse;
import uk.gov.service.notify.SendLetterResponse;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@ActiveProfiles({"local", "test"})
@SpringBootTest(webEnvironment = MOCK)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SuppressWarnings("PMD")
public class NotificationServiceImplTest {

    public static final Supplier<Notification> letterNotificationListSupplierBasedOnRefundRef = () -> Notification.builder()
        .id(1)
        .notificationType("LETTER")
        .reference("RF-123")
        .templateId("8833960c-4ffa-42db-806c-451a68c56e98")
        .createdBy("System")
        .dateUpdated(new Date())
        .contactDetails(getContactLetter())
        .build();
    @InjectMocks
    private NotificationServiceImpl notificationServiceImpl;
    @Mock
    private NotificationRepository notificationRepository;
    private ContactDetails contactDetails;
    @Spy
    private NotificationResponseMapper notificationResponseMapper;

    @Mock
    private IdamServiceImpl idamService;

    @MockBean
    @Qualifier("restTemplateIdam")
    private RestTemplate restTemplateIdam;
    @Value("${idam.api.url}")
    private String idamBaseUrl;

    @Mock
    @Qualifier("Email")
    private NotificationClientApi notificationEmailClient;

    @Mock
    @Qualifier("Letter")
    private NotificationClientApi notificationLetterClient;

    @Mock
    private EmailNotificationMapper emailNotificationMapper;

    @Mock
    private LetterNotificationMapper letterNotificationMapper;

    @Mock
    private MultiValueMap<String, String> map;

    public static final String GET_REFUND_LIST_CCD_CASE_USER_ID1 = "1f2b7025-0f91-4737-92c6-b7a9baef14c6";

    private static ContactDetails getContactLetter() {

        return ContactDetails.contactDetailsWith()
            .id(1)
            .addressLine("11 King street")
            .city("london")
            .country("UK")
            .email(null)
            .dateCreated(new Date())
            .postcode("e146kk")
            .county("london")
            .dateUpdated(new Date())
            .createdBy("e30ccf3a-8457-4e45-b251-74a346e7ec88")
            .build();
    }

    public static final Supplier<Notification> emailNotificationListSupplierBasedOnRefundRef = () -> Notification.builder()
        .id(1)
        .notificationType("EMAIL")
        .reference("RF-124")
        .templateId("8833960c-4ffa-42db-806c-451a68c56e98")
        .createdBy("e30ccf3a-8457-4e45-b251-74a346e7ec88")
        .dateUpdated(new Date())
        .contactDetails(getContactEmail())
        .build();

    private static ContactDetails getContactEmail() {

        return ContactDetails.contactDetailsWith()
            .id(1)
            .addressLine(null)
            .city(null)
            .country(null)
            .email("test@hmcts.net")
            .dateCreated(new Date())
            .postcode(null)
            .county(null)
            .dateUpdated(new Date())
            .createdBy("e30ccf3a-8457-4e45-b251-74a346e7ec88")
            .build();
    }
    public void mockUserinfoCall(IdamUserIdResponse idamUserIdResponse) {
        UriComponentsBuilder builderForUserInfo = UriComponentsBuilder.fromUriString(
            idamBaseUrl + IdamServiceImpl.USERID_ENDPOINT);
        ResponseEntity<IdamUserIdResponse> responseEntity = new ResponseEntity<>(idamUserIdResponse, HttpStatus.OK);
        when(restTemplateIdam.exchange(
            eq(builderForUserInfo.toUriString()),
            any(HttpMethod.class),
            any(HttpEntity.class),
            eq(IdamUserIdResponse.class)
        )).thenReturn(responseEntity);
    }

    public static final Supplier<IdamUserIdResponse> idamUserIDResponseSupplier = () -> IdamUserIdResponse.idamUserIdResponseWith()
        .familyName("mock-Surname")
        .givenName("mock-ForeName")
        .name("mock-ForeName mock-Surname")
        .sub("mockfullname@gmail.com")
        .roles(List.of("payments-refund", "payments-refund-approver", "refund-admin"))
        .uid(GET_REFUND_LIST_CCD_CASE_USER_ID1)
        .build();


    @Test
  public   void throwNotificationListEmptyExceptionWhenNotificationListIsEmpty() {
        when(notificationRepository.findByReferenceOrderByDateUpdatedDesc(anyString())).thenReturn(Optional.empty());

        assertThrows(NotificationListEmptyException.class, () -> notificationServiceImpl.
            getNotification(null));
    }

    @Test
    public void testNotificationListForLetterNotificationForGivenRefundReference() {

        when(notificationRepository.findByReferenceOrderByDateUpdatedDesc(anyString())).thenReturn(Optional.ofNullable(List.of(
            letterNotificationListSupplierBasedOnRefundRef.get())));

        NotificationResponseDto notificationListDtoResponse = notificationServiceImpl.getNotification("Notify-123");

        assertNotNull(notificationListDtoResponse);
        assertEquals(1, notificationListDtoResponse.getNotifications().size());
        assertEquals("LETTER", notificationListDtoResponse.getNotifications().get(0).getNotificationType());
        assertEquals("RF-123", notificationListDtoResponse.getNotifications().get(0).getReference());

    }

    @Test
   public  void givenEmptyNotificationList_whenGetNotification_thenNotificationListEmptyExceptionIsReceived() {

        when(notificationRepository.findByReferenceOrderByDateUpdatedDesc(anyString())).thenReturn(Optional.empty());

        Exception exception = assertThrows(
            NotificationListEmptyException.class,
            () -> notificationServiceImpl.getNotification(
                null
            )
        );
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains("Notification has not been sent for this refund"));
    }

    @Test
    void testNotificationListForEmailNotificationForGivenRefundReference() {

        when(notificationRepository.findByReferenceOrderByDateUpdatedDesc(anyString())).thenReturn(Optional.ofNullable(List.of(
            emailNotificationListSupplierBasedOnRefundRef.get())));

        NotificationResponseDto notificationListDtoResponse = notificationServiceImpl.getNotification("Notify-124");

        assertNotNull(notificationListDtoResponse);
        assertEquals(1, notificationListDtoResponse.getNotifications().size());
        assertEquals("EMAIL", notificationListDtoResponse.getNotifications().get(0).getNotificationType());
        assertEquals("RF-124", notificationListDtoResponse.getNotifications().get(0).getReference());

    }

    @Test
    void testsendEmailNotificationForRefundNotificationEmailRequest() throws NotificationClientException {
        mockUserinfoCall(idamUserIDResponseSupplier.get());
        RefundNotificationEmailRequest request = RefundNotificationEmailRequest.refundNotificationEmailRequestWith()
            .notificationType(NotificationType.EMAIL)
            .templateId("test")
            .reference("REF-123")
            .recipientEmailAddress("test@test.com")
            .personalisation(
                Personalisation.personalisationRequestWith().ccdCaseNumber("123").refundLagTime(1).serviceMailBox("test@test.com").serviceUrl("test.com").refundReference("test").build())
            .build();

        SendEmailResponse response = new SendEmailResponse("{\"content\":{\"body\":\"Hello Unknown, your reference is string\\r\\n\\r\\nRefund Approved\\" +
                                                               "r\\n\\r\\nThanks\",\"from_email\":\"test@gov.uk\",\"subject\":" +
                                                               "\"Refund Notification Approval\"},\"id\":\"10f101e0-6ab8-4a83-8ebd-124d648dd282\"," +
                                                               "\"reference\":\"string\",\"scheduled_for\":null,\"template\":" +
                                                               "{\"id\":\"10f101e0-6ab8-4a83-8ebd-124d648dd282\",\"uri\":" +
                                                               "\"https://api.notifications.service.gov.uk/services\"" +
                                                               ",\"version\":1},\"uri\":\"https://api.notifications.service.gov.uk\"}\n");
        Notification notification = Notification.builder().build();

        when(notificationEmailClient.sendEmail(any(), any(), any(), any())).thenReturn(response);
        when(emailNotificationMapper.emailResponseMapper(any(),any())).thenReturn(notification);
        when(notificationRepository.save(notification)).thenReturn(notification);


        response = notificationServiceImpl.sendEmailNotification(request,any());

        assertEquals("Refund Notification Approval", response.getSubject());
        assertEquals("test@gov.uk", response.getFromEmail().get());


    }

    @Test
    void testsendLetterNotificationForRefundNotificationLetterRequest() throws NotificationClientException {
        mockUserinfoCall(idamUserIDResponseSupplier.get());
        RefundNotificationLetterRequest request = RefundNotificationLetterRequest.refundNotificationLetterRequestWith()
            .notificationType(NotificationType.LETTER)
            .templateId("test")
            .reference("REF-123")
            .recipientPostalAddress(RecipientPostalAddress.recipientPostalAddressWith()
                                        .addressLine("test")
                                        .city("test")
                                        .county("test")
                                        .country("test")
                                        .postalCode("TE ST1")
                                        .build())
            .personalisation(
                Personalisation.personalisationRequestWith().ccdCaseNumber("123").refundLagTime(1).serviceMailBox("test@test.com").serviceUrl("test.com").refundReference("test").build())
            .build();

        SendLetterResponse response = new SendLetterResponse("{\"content\":{\"body\":\"Hello Unknown\\r\\n\\r\\nRefund Approved on 2022-01-01\"," +
                                                                 "\"subject\":\"Refund Notification\"},\"id\":\"0f101e0-6ab8-4a83-8ebd-124d648dd282\"," +
                                                                 "\"reference\":\"string\",\"scheduled_for\":null,\"template\":{\"id\":" +
                                                                 "\"0f101e0-6ab8-4a83-8ebd-124d648dd282\",\"uri\":" +
                                                                 "\"https://api.notifications.service\"," +
                                                                 "\"version\":1},\"uri\":\"https://api.notifications.service\"}\n");
        Notification notification = Notification.builder().build();

        when(notificationLetterClient.sendLetter(any(), any(), any())).thenReturn(response);
        when(letterNotificationMapper.letterResponseMapper(any(),any(),any())).thenReturn(notification);
        when(notificationRepository.save(notification)).thenReturn(notification);


        response = notificationServiceImpl.sendLetterNotification(request,any());
        assertEquals("Refund Notification", response.getSubject());


    }


    @Test
    public void throwsInvalidAddressExceptionWhenInvalidPostCodeProvidedInRequest() throws Exception {
        mockUserinfoCall(idamUserIDResponseSupplier.get());
        String errorMessage = "Status code: 400 {\"errors\":[{\"error\":\"BadRequestError\"," +
            "\"message\":\"Must be a real UK postcode\"}],\"status_code\":400}\n";

        RefundNotificationLetterRequest request = RefundNotificationLetterRequest.refundNotificationLetterRequestWith()
            .notificationType(NotificationType.LETTER)
            .templateId("test")
            .reference("REF-123")
            .recipientPostalAddress(RecipientPostalAddress.recipientPostalAddressWith()
                                        .addressLine("test")
                                        .city("test")
                                        .county("test")
                                        .country("test")
                                        .postalCode("TE ST1")
                                        .build())
            .personalisation(
                Personalisation.personalisationRequestWith().ccdCaseNumber("123").refundLagTime(1).serviceMailBox("test@test.com").serviceUrl("test.com").refundReference("test").build())
            .build();

        when(notificationLetterClient.sendLetter(any(), any(), any())).thenThrow(new NotificationClientException(errorMessage));
        assertThrows(InvalidAddressException.class, () -> notificationServiceImpl.sendLetterNotification(request, any()
        ));
    }

    @Test
    public void throwsInvalidTemplateIdExcetionWhenInvalidTemplateIdProvidedInRequest() throws Exception {

        mockUserinfoCall(idamUserIDResponseSupplier.get());
        String errorMessage = "Status code: 400 {\"errors\":[{\"error\":\"BadRequestError\"," +
            "\"message\":\"template_id is not a valid UUID\"}],\"status_code\":400}\n";
        RefundNotificationLetterRequest request = RefundNotificationLetterRequest.refundNotificationLetterRequestWith()
            .notificationType(NotificationType.LETTER)
            .templateId("test")
            .reference("REF-123")
            .recipientPostalAddress(RecipientPostalAddress.recipientPostalAddressWith()
                                        .addressLine("test")
                                        .city("test")
                                        .county("test")
                                        .country("test")
                                        .postalCode("TE ST1")
                                        .build())
            .personalisation(
                Personalisation.personalisationRequestWith().ccdCaseNumber("123").refundLagTime(1).serviceMailBox("test@test.com").serviceUrl("test.com").refundReference("test").build())
            .build();

        when(notificationLetterClient.sendLetter(any(), any(), any())).thenThrow(new NotificationClientException(errorMessage));
        assertThrows(InvalidTemplateId.class, () -> notificationServiceImpl.sendLetterNotification(request, any()
        ));
    }

    @Test
    public void throwsInvalidApiKeyExceptionnWhenInvalidApiKeyInRequest() throws Exception {

        mockUserinfoCall(idamUserIDResponseSupplier.get());
        String errorMessage = "Status code: 403 {\"errors\":[{\"error\":\"BadRequestError\"," +
            "\"message\":\"Invalid api key\"}],\"status_code\":400}\n";
        RefundNotificationEmailRequest request = RefundNotificationEmailRequest.refundNotificationEmailRequestWith()
            .notificationType(NotificationType.EMAIL)
            .templateId("test")
            .reference("REF-123")
            .recipientEmailAddress("test@test.com")
            .personalisation(
                Personalisation.personalisationRequestWith().ccdCaseNumber("123").refundLagTime(1).serviceMailBox("test@test.com").serviceUrl("test.com").refundReference("test").build())
            .build();

        when(notificationEmailClient.sendEmail(any(), any(),any(),any())).thenThrow(new NotificationClientException(errorMessage));
        assertThrows(InvalidApiKeyException.class, () -> notificationServiceImpl.sendEmailNotification(request, any()
        ));
    }
}
