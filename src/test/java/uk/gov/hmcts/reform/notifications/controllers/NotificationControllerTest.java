package uk.gov.hmcts.reform.notifications.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.notifications.service.NotificationServiceImplTest;
import uk.gov.hmcts.reform.notifications.config.security.idam.IdamServiceImpl;
import uk.gov.hmcts.reform.notifications.dtos.enums.NotificationType;
import uk.gov.hmcts.reform.notifications.dtos.request.Personalisation;
import uk.gov.hmcts.reform.notifications.dtos.request.RecipientPostalAddress;
import uk.gov.hmcts.reform.notifications.dtos.request.RefundNotificationEmailRequest;
import uk.gov.hmcts.reform.notifications.dtos.request.RefundNotificationLetterRequest;
import uk.gov.hmcts.reform.notifications.dtos.response.IdamUserIdResponse;
import uk.gov.hmcts.reform.notifications.dtos.response.NotificationResponseDto;
import uk.gov.hmcts.reform.notifications.model.Notification;
import uk.gov.hmcts.reform.notifications.repository.NotificationRepository;
import uk.gov.hmcts.reform.notifications.service.NotificationServiceImpl;
import uk.gov.service.notify.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

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

    @Mock
    private IdamServiceImpl idamService;

    @MockBean
    @Qualifier("restTemplateIdam")
    private RestTemplate restTemplateIdam;
    @Value("${idam.api.url}")
    private String idamBaseUrl;
    @Mock
    private MultiValueMap<String, String> map;

    @MockBean
    @Qualifier("restTemplatePayment")
    private RestTemplate restTemplatePayment;

    @MockBean
    private AuthTokenGenerator authTokenGenerator;

    @MockBean
    private ClientRegistrationRepository clientRegistrationRepository;

    @MockBean
    private JwtDecoder jwtDecoder;

    @Autowired
    private ObjectMapper objectMapper;

    private ObjectMapper mapper = new ObjectMapper();

    public static final String GET_REFUND_LIST_CCD_CASE_USER_ID1 = "1f2b7025-0f91-4737-92c6-b7a9baef14c6";

    @BeforeEach
    void setUp() {
        mockMvc = webAppContextSetup(webApplicationContext).build();
    }

    private static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
    public void createEmailNotificationReturnSuccess() throws Exception {
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
        when(notificationRepository.save(notification)).thenReturn(notification);

        MvcResult result = mockMvc.perform(post("/notifications/email")
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
        mockUserinfoCall(idamUserIDResponseSupplier.get());
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

        MvcResult result = mockMvc.perform(post("/notifications/email")
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
        mockUserinfoCall(idamUserIDResponseSupplier.get());
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

        MvcResult result = mockMvc.perform(post("/notifications/email")
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

        Notification notification = Notification.builder().build();


        when(notificationEmailClient.sendEmail(any(), any(), any(), any())).thenThrow(new NotificationClientException(errorMessage));
        when(notificationRepository.save(notification)).thenReturn(notification);

        MvcResult result = mockMvc.perform(post("/notifications/email")
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
        mockUserinfoCall(idamUserIDResponseSupplier.get());
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

        MvcResult result = mockMvc.perform(post("/notifications/email")
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
        mockUserinfoCall(idamUserIDResponseSupplier.get());
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

        MvcResult result = mockMvc.perform(post("/notifications/email")
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
        mockUserinfoCall(idamUserIDResponseSupplier.get());
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

        MvcResult result = mockMvc.perform(post("/notifications/email")
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
        mockUserinfoCall(idamUserIDResponseSupplier.get());
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

        MvcResult result = mockMvc.perform(post("/notifications/letter")
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
        mockUserinfoCall(idamUserIDResponseSupplier.get());
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
        when(notificationRepository.save(notification)).thenReturn(notification);

        MvcResult result = mockMvc.perform(post("/notifications/letter")
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
        mockUserinfoCall(idamUserIDResponseSupplier.get());
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

        MvcResult result = mockMvc.perform(post("/notifications/letter")
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
        mockUserinfoCall(idamUserIDResponseSupplier.get());
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

        MvcResult result = mockMvc.perform(post("/notifications/letter")
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
        mockUserinfoCall(idamUserIDResponseSupplier.get());
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

        MvcResult result = mockMvc.perform(post("/notifications/letter")
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
        mockUserinfoCall(idamUserIDResponseSupplier.get());
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

        MvcResult result = mockMvc.perform(post("/notifications/letter")
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
        mockUserinfoCall(idamUserIDResponseSupplier.get());
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

        MvcResult result = mockMvc.perform(post("/notifications/letter")
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
        mockUserinfoCall(idamUserIDResponseSupplier.get());
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

        MvcResult result = mockMvc.perform(post("/notifications/letter")
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
        mockUserinfoCall(idamUserIDResponseSupplier.get());
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

        MvcResult result = mockMvc.perform(post("/notifications/letter")
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
    void testGetNotificationListForLetterWhenValidRefundReferenceProvided() throws Exception {

        //mock userinfo call
        mockUserinfoCall(idamUserIDResponseSupplier.get());
        when(notificationRepository.findByReferenceOrderByDateUpdatedDesc(any()
        ))
            .thenReturn(Optional.ofNullable(List.of(
                NotificationServiceImplTest.letterNotificationListSupplierBasedOnRefundRef.get())));

        MvcResult mvcResult = mockMvc.perform(get("/notifications/RF-123")
                                                  .header("Authorization", "user")
                                                  .header("ServiceAuthorization", "Services")
                                                  .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk()).andReturn();

        NotificationResponseDto notificationResponseDto = mapper.readValue(
            mvcResult.getResponse().getContentAsString(), NotificationResponseDto.class
        );
        Assertions.assertEquals("LETTER",notificationResponseDto.getNotifications().get(0).getNotificationType());
    }

    @Test
    void testGetNotificationListForEmailWhenValidRefundReferenceProvided() throws Exception {

        //mock userinfo call
        mockUserinfoCall(idamUserIDResponseSupplier.get());
        when(notificationRepository.findByReferenceOrderByDateUpdatedDesc(any()
        ))
            .thenReturn(Optional.ofNullable(List.of(
                NotificationServiceImplTest.emailNotificationListSupplierBasedOnRefundRef.get())));

        MvcResult mvcResult = mockMvc.perform(get("/notifications/RF-124")
                                                  .header("Authorization", "user")
                                                  .header("ServiceAuthorization", "Services")
                                                  .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk()).andReturn();

        NotificationResponseDto notificationResponseDto = mapper.readValue(
            mvcResult.getResponse().getContentAsString(), NotificationResponseDto.class
        );
        Assertions.assertEquals("EMAIL",notificationResponseDto.getNotifications().get(0).getNotificationType());
    }

    @Test
    void returnException404WhenGetNotificationReturnEmptyNotificationList() throws Exception {

        //mock userinfo call
        mockUserinfoCall(idamUserIDResponseSupplier.get());
        Optional<List<Notification>> notificationList=Optional.empty();
        when(notificationRepository.findByReferenceOrderByDateUpdatedDesc(any()
        ))
            .thenReturn(notificationList);

        MvcResult mvcResult = mockMvc.perform(get("/notifications/RF-124")
                                                  .header("Authorization", "user")
                                                  .header("ServiceAuthorization", "Services")
                                                  .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound()).andReturn();

        Assertions.assertEquals("Notification has not been sent for this refund", mvcResult.getResolvedException().getMessage());
    }
}
