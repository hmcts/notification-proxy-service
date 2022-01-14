package uk.gov.hmcts.reform.notifications.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.notifications.dtos.enums.NotificationType;
import uk.gov.hmcts.reform.notifications.dtos.request.Personalisation;
import uk.gov.hmcts.reform.notifications.dtos.request.RecipientPostalAddress;
import uk.gov.hmcts.reform.notifications.dtos.request.RefundNotificationEmailRequest;
import uk.gov.hmcts.reform.notifications.dtos.request.RefundNotificationLetterRequest;
import uk.gov.hmcts.reform.notifications.model.Notification;
import uk.gov.hmcts.reform.notifications.repository.NotificationRepository;
import uk.gov.hmcts.reform.notifications.service.NotificationServiceImpl;
import uk.gov.service.notify.*;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"local", "test"})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SuppressWarnings("PMD")
public class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private WebApplicationContext webApplicationContext;
    @Mock
    private NotificationServiceImpl notificationService;
    @MockBean
    @Qualifier("Email")
    private NotificationClientApi notificationEmailClient;

    @MockBean
    @Qualifier("Letter")
    private NotificationClientApi notificationLetterClient;

    @MockBean
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationController notificationController;

    private static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Test
    public void createEmailNotificationReturnSuccess() throws Exception {
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
//        when(notificationClient.sendEmail(any(),any(), any(),any(),any())).thenReturn(response);
        when(notificationRepository.save(notification)).thenReturn(notification);

        MvcResult result = mockMvc.perform(post("/emailNotification")
                                               .content(asJsonString(request))
                                               .header("Authorization", "user")
                                               .header("ServiceAuthorization", "Services")
                                               .contentType(MediaType.APPLICATION_JSON)
                                               .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andReturn();
    }

    @Test
    public void createEmailNotificationReturn500ThrowsRestrictedApiKeyException() throws Exception {
        String errorMessage = "Status code: 400 {\"errors\":[{\"error\":\"BadRequestError\"," +
            "\"message\":\"Can\\u2019t send to this recipient using a team-only API key\"}],\"status_code\":400}\n";
        RefundNotificationEmailRequest request = RefundNotificationEmailRequest.refundNotificationEmailRequestWith()
            .notificationType(NotificationType.EMAIL)
            .templateId("test")
            .reference("REF-123")
            .recipientEmailAddress("test@test.com")
            .personalisation(
                Personalisation.personalisationRequestWith().ccdCaseNumber("123").refundLagTime(1).serviceMailBox("test@test.com").serviceUrl("test.com").refundReference("test").build())
            .build();

        Notification notification = Notification.builder().build();


        when(notificationEmailClient.sendEmail(any(), any(), any(), any())).thenThrow(new NotificationClientException(errorMessage));
        when(notificationRepository.save(notification)).thenReturn(notification);

        MvcResult result = mockMvc.perform(post("/emailNotification")
                                               .content(asJsonString(request))
                                               .header("Authorization", "user")
                                               .header("ServiceAuthorization", "Services")
                                               .contentType(MediaType.APPLICATION_JSON)
                                               .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isInternalServerError())
            .andReturn();

        Assertions.assertEquals("Internal Server Error, restricted API key",result.getResolvedException().getMessage());
    }


    @Test
    public void createEmailNotificationReturn422ThrowsInvalidTemplateId() throws Exception {
        String errorMessage = "Status code: 400 {\"errors\":[{\"error\":\"BadRequestError\"," +
            "\"message\":\"template_id is not a valid UUID\"}],\"status_code\":400}\n";
        RefundNotificationEmailRequest request = RefundNotificationEmailRequest.refundNotificationEmailRequestWith()
            .notificationType(NotificationType.EMAIL)
            .templateId("test")
            .reference("REF-123")
            .recipientEmailAddress("test@test.com")
            .personalisation(
                Personalisation.personalisationRequestWith().ccdCaseNumber("123").refundLagTime(1).serviceMailBox("test@test.com").serviceUrl("test.com").refundReference("test").build())
            .build();

        Notification notification = Notification.builder().build();


        when(notificationEmailClient.sendEmail(any(), any(), any(), any())).thenThrow(new NotificationClientException(errorMessage));
        when(notificationRepository.save(notification)).thenReturn(notification);

        MvcResult result = mockMvc.perform(post("/emailNotification")
                                               .content(asJsonString(request))
                                               .header("Authorization", "user")
                                               .header("ServiceAuthorization", "Services")
                                               .contentType(MediaType.APPLICATION_JSON)
                                               .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnprocessableEntity())
            .andReturn();

        Assertions.assertEquals("Invalid Template ID",result.getResolvedException().getMessage());
    }

    @Test
    public void createEmailNotificationReturn500ThrowsInvalidApiKeyException() throws Exception {
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

        Notification notification = Notification.builder().build();


        when(notificationEmailClient.sendEmail(any(), any(), any(), any())).thenThrow(new NotificationClientException(errorMessage));
        when(notificationRepository.save(notification)).thenReturn(notification);

        MvcResult result = mockMvc.perform(post("/emailNotification")
                                               .content(asJsonString(request))
                                               .header("Authorization", "user")
                                               .header("ServiceAuthorization", "Services")
                                               .contentType(MediaType.APPLICATION_JSON)
                                               .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isInternalServerError())
            .andReturn();

        Assertions.assertEquals("Internal Server Error, invalid API key",result.getResolvedException().getMessage());
    }

    @Test
    public void createEmailNotificationReturn500ThrowsExceededRequestLimitException() throws Exception {
        String errorMessage = "Status code: 429 {\"errors\":[{\"error\":\"BadRequestError\"," +
            "\"message\":\"Invalid api key\"}],\"status_code\":400}\n";
        RefundNotificationEmailRequest request = RefundNotificationEmailRequest.refundNotificationEmailRequestWith()
            .notificationType(NotificationType.EMAIL)
            .templateId("test")
            .reference("REF-123")
            .recipientEmailAddress("test@test.com")
            .personalisation(
                Personalisation.personalisationRequestWith().ccdCaseNumber("123").refundLagTime(1).serviceMailBox("test@test.com").serviceUrl("test.com").refundReference("test").build())
            .build();

        Notification notification = Notification.builder().build();


        when(notificationEmailClient.sendEmail(any(), any(), any(), any())).thenThrow(new NotificationClientException(errorMessage));
        when(notificationRepository.save(notification)).thenReturn(notification);

        MvcResult result = mockMvc.perform(post("/emailNotification")
                                               .content(asJsonString(request))
                                               .header("Authorization", "user")
                                               .header("ServiceAuthorization", "Services")
                                               .contentType(MediaType.APPLICATION_JSON)
                                               .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isInternalServerError())
            .andReturn();

        Assertions.assertEquals("Internal Server Error, send limit exceeded",result.getResolvedException().getMessage());
    }

    @Test
    public void createEmailNotificationReturn503ThrowsGovNotifyConnectionException() throws Exception {
        String errorMessage = "Status code: 500 {\"errors\":[{\"error\":\"BadRequestError\"," +
            "\"message\":\"Invalid api key\"}],\"status_code\":400}\n";
        RefundNotificationEmailRequest request = RefundNotificationEmailRequest.refundNotificationEmailRequestWith()
            .notificationType(NotificationType.EMAIL)
            .templateId("test")
            .reference("REF-123")
            .recipientEmailAddress("test@test.com")
            .personalisation(
                Personalisation.personalisationRequestWith().ccdCaseNumber("123").refundLagTime(1).serviceMailBox("test@test.com").serviceUrl("test.com").refundReference("test").build())
            .build();

        Notification notification = Notification.builder().build();


        when(notificationEmailClient.sendEmail(any(), any(), any(), any())).thenThrow(new NotificationClientException(errorMessage));
        when(notificationRepository.save(notification)).thenReturn(notification);

        MvcResult result = mockMvc.perform(post("/emailNotification")
                                               .content(asJsonString(request))
                                               .header("Authorization", "user")
                                               .header("ServiceAuthorization", "Services")
                                               .contentType(MediaType.APPLICATION_JSON)
                                               .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isServiceUnavailable())
            .andReturn();

        Assertions.assertEquals("Service is not available, please try again",result.getResolvedException().getMessage());
    }

    @Test
    public void createEmailNotificationReturn500ThrowsGovNotifyUnmappedException() throws Exception {
        String errorMessage = "Status code: 503 {\"errors\":[{\"error\":\"BadRequestError\"," +
            "\"message\":\"Invalid api key\"}],\"status_code\":400}\n";
        RefundNotificationEmailRequest request = RefundNotificationEmailRequest.refundNotificationEmailRequestWith()
            .notificationType(NotificationType.EMAIL)
            .templateId("test")
            .reference("REF-123")
            .recipientEmailAddress("test@test.com")
            .personalisation(
                Personalisation.personalisationRequestWith().ccdCaseNumber("123").refundLagTime(1).serviceMailBox("test@test.com").serviceUrl("test.com").refundReference("test").build())
            .build();

        Notification notification = Notification.builder().build();


        when(notificationEmailClient.sendEmail(any(), any(), any(), any())).thenThrow(new NotificationClientException(errorMessage));
        when(notificationRepository.save(notification)).thenReturn(notification);

        MvcResult result = mockMvc.perform(post("/emailNotification")
                                               .content(asJsonString(request))
                                               .header("Authorization", "user")
                                               .header("ServiceAuthorization", "Services")
                                               .contentType(MediaType.APPLICATION_JSON)
                                               .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isInternalServerError())
            .andReturn();

        Assertions.assertEquals("Internal Server Error",result.getResolvedException().getMessage());
    }


    @Test
    public void createLetterNotificationReturnSuccess() throws Exception {
        RefundNotificationLetterRequest request = RefundNotificationLetterRequest.refundNotificationLetterRequestWith()
            .notificationType(NotificationType.EMAIL)
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
//        when(notificationClient.sendEmail(any(),any(), any(),any(),any())).thenReturn(response);
        when(notificationRepository.save(notification)).thenReturn(notification);

        MvcResult result = mockMvc.perform(post("/letterNotification")
                                               .content(asJsonString(request))
                                               .header("Authorization", "user")
                                               .header("ServiceAuthorization", "Services")
                                               .contentType(MediaType.APPLICATION_JSON)
                                               .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andReturn();
    }

    @Test
    public void createLetterNotificationReturn422ThrowInvalidAddresssException() throws Exception {
        String errorMessage = "Status code: 400 {\"errors\":[{\"error\":\"BadRequestError\"," +
            "\"message\":\"Must be a real UK postcode\"}],\"status_code\":400}\n";

        RefundNotificationLetterRequest request = RefundNotificationLetterRequest.refundNotificationLetterRequestWith()
            .notificationType(NotificationType.EMAIL)
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

        Notification notification = Notification.builder().build();

        when(notificationLetterClient.sendLetter(any(), any(), any())).thenThrow(new NotificationClientException(errorMessage));
//        when(notificationClient.sendEmail(any(),any(), any(),any(),any())).thenReturn(response);
        when(notificationRepository.save(notification)).thenReturn(notification);

        MvcResult result = mockMvc.perform(post("/letterNotification")
                                               .content(asJsonString(request))
                                               .header("Authorization", "user")
                                               .header("ServiceAuthorization", "Services")
                                               .contentType(MediaType.APPLICATION_JSON)
                                               .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnprocessableEntity())
            .andReturn();

        Assertions.assertEquals("Please enter a valid/real postcode",result.getResolvedException().getMessage());

    }

    @Test
    public void createLetterNotificationReturn422ThrowInvalidAddressException2() throws Exception {
        String errorMessage = "Status code: 400 {\"errors\":[{\"error\":\"BadRequestError\"," +
            "\"message\":\"Last line of address must be a real UK postcode or another country\"}],\"status_code\":400}\n";

        RefundNotificationLetterRequest request = RefundNotificationLetterRequest.refundNotificationLetterRequestWith()
            .notificationType(NotificationType.EMAIL)
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

        Notification notification = Notification.builder().build();

        when(notificationLetterClient.sendLetter(any(), any(), any())).thenThrow(new NotificationClientException(errorMessage));
        when(notificationRepository.save(notification)).thenReturn(notification);

        MvcResult result = mockMvc.perform(post("/letterNotification")
                                               .content(asJsonString(request))
                                               .header("Authorization", "user")
                                               .header("ServiceAuthorization", "Services")
                                               .contentType(MediaType.APPLICATION_JSON)
                                               .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnprocessableEntity())
            .andReturn();

        Assertions.assertEquals("Please enter a valid/real postcode",result.getResolvedException().getMessage());

    }

    @Test
    public void createLetterNotificationReturn422ThrowInvalidTemplateID() throws Exception {
        String errorMessage = "Status code: 400 {\"errors\":[{\"error\":\"BadRequestError\"," +
            "\"message\":\"template_id is not a valid UUID\"}],\"status_code\":400}\n";

        RefundNotificationLetterRequest request = RefundNotificationLetterRequest.refundNotificationLetterRequestWith()
            .notificationType(NotificationType.EMAIL)
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

        Notification notification = Notification.builder().build();

        when(notificationLetterClient.sendLetter(any(), any(), any())).thenThrow(new NotificationClientException(errorMessage));
        when(notificationRepository.save(notification)).thenReturn(notification);

        MvcResult result = mockMvc.perform(post("/letterNotification")
                                               .content(asJsonString(request))
                                               .header("Authorization", "user")
                                               .header("ServiceAuthorization", "Services")
                                               .contentType(MediaType.APPLICATION_JSON)
                                               .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnprocessableEntity())
            .andReturn();

        Assertions.assertEquals("Invalid Template ID",result.getResolvedException().getMessage());

    }

    @Test
    public void createLetterNotificationReturn422ThrowRestrictedApiKeyException() throws Exception {
        String errorMessage = "Status code: 400 {\"errors\":[{\"error\":\"BadRequestError\"," +
            "\"message\":\"something else\"}],\"status_code\":400}\n";

        RefundNotificationLetterRequest request = RefundNotificationLetterRequest.refundNotificationLetterRequestWith()
            .notificationType(NotificationType.EMAIL)
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

        Notification notification = Notification.builder().build();

        when(notificationLetterClient.sendLetter(any(), any(), any())).thenThrow(new NotificationClientException(errorMessage));
        when(notificationRepository.save(notification)).thenReturn(notification);

        MvcResult result = mockMvc.perform(post("/letterNotification")
                                               .content(asJsonString(request))
                                               .header("Authorization", "user")
                                               .header("ServiceAuthorization", "Services")
                                               .contentType(MediaType.APPLICATION_JSON)
                                               .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isInternalServerError())
            .andReturn();

        Assertions.assertEquals("Internal Server Error, restricted API key",result.getResolvedException().getMessage());

    }


    @Test
    public void createLetterNotificationReturn500ThrowRestrictedApiKeyException() throws Exception {
        String errorMessage = "Status code: 403 {\"errors\":[{\"error\":\"BadRequestError\"," +
            "\"message\":\"Incorrect api key\"}],\"status_code\":400}\n";

        RefundNotificationLetterRequest request = RefundNotificationLetterRequest.refundNotificationLetterRequestWith()
            .notificationType(NotificationType.EMAIL)
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

        Notification notification = Notification.builder().build();

        when(notificationLetterClient.sendLetter(any(), any(), any())).thenThrow(new NotificationClientException(errorMessage));
        when(notificationRepository.save(notification)).thenReturn(notification);

        MvcResult result = mockMvc.perform(post("/letterNotification")
                                               .content(asJsonString(request))
                                               .header("Authorization", "user")
                                               .header("ServiceAuthorization", "Services")
                                               .contentType(MediaType.APPLICATION_JSON)
                                               .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isInternalServerError())
            .andReturn();

        Assertions.assertEquals("Internal Server Error, invalid API key",result.getResolvedException().getMessage());

    }

    @Test
    public void createLetterNotificationReturn500ThrowExceededRequestLimitException() throws Exception {
        String errorMessage = "Status code: 429 {\"errors\":[{\"error\":\"BadRequestError\"," +
            "\"message\":\"Incorrect api key\"}],\"status_code\":400}\n";

        RefundNotificationLetterRequest request = RefundNotificationLetterRequest.refundNotificationLetterRequestWith()
            .notificationType(NotificationType.EMAIL)
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

        Notification notification = Notification.builder().build();

        when(notificationLetterClient.sendLetter(any(), any(), any())).thenThrow(new NotificationClientException(errorMessage));
        when(notificationRepository.save(notification)).thenReturn(notification);

        MvcResult result = mockMvc.perform(post("/letterNotification")
                                               .content(asJsonString(request))
                                               .header("Authorization", "user")
                                               .header("ServiceAuthorization", "Services")
                                               .contentType(MediaType.APPLICATION_JSON)
                                               .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isInternalServerError())
            .andReturn();
        Assertions.assertEquals("Internal Server Error, send limit exceeded",result.getResolvedException().getMessage());

    }

    @Test
    public void createLetterNotificationReturn503ThrowGovNotifyConnectionException() throws Exception {
        String errorMessage = "Status code: 500 {\"errors\":[{\"error\":\"BadRequestError\"," +
            "\"message\":\"Incorrect api key\"}],\"status_code\":400}\n";

        RefundNotificationLetterRequest request = RefundNotificationLetterRequest.refundNotificationLetterRequestWith()
            .notificationType(NotificationType.EMAIL)
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

        Notification notification = Notification.builder().build();

        when(notificationLetterClient.sendLetter(any(), any(), any())).thenThrow(new NotificationClientException(errorMessage));
        when(notificationRepository.save(notification)).thenReturn(notification);

        MvcResult result = mockMvc.perform(post("/letterNotification")
                                               .content(asJsonString(request))
                                               .header("Authorization", "user")
                                               .header("ServiceAuthorization", "Services")
                                               .contentType(MediaType.APPLICATION_JSON)
                                               .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isServiceUnavailable())
            .andReturn();

        Assertions.assertEquals("Service is not available, please try again",result.getResolvedException().getMessage());
    }

    @Test
    public void createLetterNotificationReturn500ThrowGovNotifyUnmappedException() throws Exception {
        String errorMessage = "Status code: 503 {\"errors\":[{\"error\":\"BadRequestError\"," +
            "\"message\":\"Incorrect api key\"}],\"status_code\":400}\n";

        RefundNotificationLetterRequest request = RefundNotificationLetterRequest.refundNotificationLetterRequestWith()
            .notificationType(NotificationType.EMAIL)
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

        Notification notification = Notification.builder().build();

        when(notificationLetterClient.sendLetter(any(), any(), any())).thenThrow(new NotificationClientException(errorMessage));
        when(notificationRepository.save(notification)).thenReturn(notification);

        MvcResult result = mockMvc.perform(post("/letterNotification")
                                               .content(asJsonString(request))
                                               .header("Authorization", "user")
                                               .header("ServiceAuthorization", "Services")
                                               .contentType(MediaType.APPLICATION_JSON)
                                               .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isInternalServerError())
            .andReturn();

        Assertions.assertEquals("Internal Server Error",result.getResolvedException().getMessage());

    }


}
