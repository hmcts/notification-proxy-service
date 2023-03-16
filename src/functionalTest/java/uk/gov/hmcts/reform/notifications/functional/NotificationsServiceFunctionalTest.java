package uk.gov.hmcts.reform.notifications.functional;

import java.math.BigDecimal;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.TestInstance;
import org.springframework.http.HttpStatus;

import io.restassured.response.Response;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.notifications.dtos.enums.NotificationType;
import uk.gov.hmcts.reform.notifications.dtos.request.DocPreviewRequest;
import uk.gov.hmcts.reform.notifications.dtos.request.Personalisation;
import uk.gov.hmcts.reform.notifications.dtos.request.RecipientPostalAddress;
import uk.gov.hmcts.reform.notifications.dtos.request.RefundNotificationEmailRequest;
import uk.gov.hmcts.reform.notifications.dtos.request.RefundNotificationLetterRequest;
import uk.gov.hmcts.reform.notifications.dtos.response.FromTemplateContact;
import uk.gov.hmcts.reform.notifications.dtos.response.MailAddress;
import uk.gov.hmcts.reform.notifications.dtos.response.NotificationTemplatePreviewResponse;
import uk.gov.hmcts.reform.notifications.functional.config.IdamService;
import uk.gov.hmcts.reform.notifications.functional.config.NotificationsTestService;
import uk.gov.hmcts.reform.notifications.functional.config.S2sTokenService;
import uk.gov.hmcts.reform.notifications.functional.config.TestConfigProperties;
import uk.gov.hmcts.reform.notifications.model.TemplatePreviewDto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.NO_CONTENT;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

@ActiveProfiles({"functional", "liberataMock"})
@ContextConfiguration(classes = TestContextConfiguration.class)
@RunWith(SpringIntegrationSerenityRunner.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
public class NotificationsServiceFunctionalTest {

    @Value("${notification.email-to-reply}")
    private String emailReplyToId;

    @Value("${notification.service-mailbox}")
    private String serviceMailBox;

    @Value("${notification.service-url}")
    private String serviceUrl;

    @Value("${notify.letter.template}")
    private String letterTemplateId;

    @Value("${notify.email.template}")
    private String emailTemplateId;

    @Value("${notify.template.cheque-po-cash.letter}")
    private String chequePoCashLetterTemplateId;

    @Value("${notify.template.cheque-po-cash.email}")
    private String chequePoCashEmailTemplateId;

    @Value("${notify.template.card-pba.letter}")
    private String cardPbaLetterTemplateId;

    @Value("${notify.template.card-pba.email}")
    private String cardPbaEmailTemplateId;

    @Autowired
    private TestConfigProperties testConfigProperties;

    @Autowired
    private IdamService idamService;

    @Autowired
    private S2sTokenService s2sTokenService;

    @Autowired
    private NotificationsTestService notificationsTestService;

    private String serviceTokenPayBubble;

    private String userTokenPaymentRefundApprover;

    private boolean isTokensInitialized;

    private static final String CCD_CASE_NUMBER = "1234567890123456";

    private static final String CITY = "London";

    private static final String COUNTY = "London";

    @BeforeAll
    public void setUp() {

        if (!isTokensInitialized) {
            userTokenPaymentRefundApprover = idamService.createUserWithSearchScope
                ("idam.user.ccpayrefundsapi@hmcts.net").getAuthorisationToken();
            serviceTokenPayBubble =
                s2sTokenService.getS2sToken("ccpay_bubble", testConfigProperties.s2sPayBubble);

            isTokensInitialized = true;
        }
    }

    @Test
    public void sendEmailNotificationRequest() {

        String reference = "FunctionalTest1";
        RefundNotificationEmailRequest refundNotificationEmailRequest = RefundNotificationEmailRequest.refundNotificationEmailRequestWith()
            .templateId(emailTemplateId)
            .recipientEmailAddress("vat12@mailinator.com")
            .reference(reference)
            .emailReplyToId(emailReplyToId)
            .notificationType(NotificationType.EMAIL)
            .serviceName("Probate")
            .personalisation(Personalisation.personalisationRequestWith().ccdCaseNumber(CCD_CASE_NUMBER).refundReference("RF-1234-1234-1234-1234").refundAmount(
                BigDecimal.valueOf(10)).refundReason("RR001").build())
            .build();

        final Response responseNotificationEmail = notificationsTestService.postEmailNotification(
            userTokenPaymentRefundApprover ,
            serviceTokenPayBubble ,
            testConfigProperties.baseTestUrl,
            refundNotificationEmailRequest
        );

        assertThat(responseNotificationEmail.getStatusCode()).isEqualTo(HttpStatus.CREATED.value());
        deleteNotifications(reference);
    }

    @Test
    public void sendEmailNotificationRequestWithReasonUnableToApplyRefundToCard() {

        String reference = "FunctionalTest1";
     //   sendEmailNotificationRequest();
        RefundNotificationEmailRequest refundNotificationEmailRequest = RefundNotificationEmailRequest.refundNotificationEmailRequestWith()
            .templateId(emailTemplateId)
            .recipientEmailAddress("vat12@mailinator.com")
            .reference(reference)
            .notificationType(NotificationType.EMAIL)
            .serviceName("Probate")
            .personalisation(Personalisation.personalisationRequestWith()
                                 .ccdCaseNumber(CCD_CASE_NUMBER)
                                 .refundReference(reference)
                                 .refundAmount(BigDecimal.valueOf(10))
                                 .refundReason("Unable to apply refund to Card")
                                 .build())
            .build();

        final Response responseNotificationEmail = notificationsTestService.postEmailNotification(
            userTokenPaymentRefundApprover ,
            serviceTokenPayBubble ,
            testConfigProperties.baseTestUrl,
            refundNotificationEmailRequest
        );

        assertThat(responseNotificationEmail.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    @Test
    public void sendLetterNotificationRequest() {

        String reference ="FunctionalTest2";
        RefundNotificationLetterRequest refundNotificationLetterRequest = RefundNotificationLetterRequest.refundNotificationLetterRequestWith()
            .templateId(letterTemplateId)
            .recipientPostalAddress(RecipientPostalAddress.recipientPostalAddressWith()
                                        .addressLine("102 Petty France")
                                        .city(CITY)
                                        .county(COUNTY)
                                        .country("England")
                                        .postalCode("SW1H 9AJ")
                                        .build())
            .reference(reference)
            .notificationType(NotificationType.LETTER)
            .serviceName("Probate")
            .personalisation(Personalisation.personalisationRequestWith().ccdCaseNumber(CCD_CASE_NUMBER).refundReference("RF-1234-1234-1234-1234").refundAmount(
                BigDecimal.valueOf(10)).refundReason("RR001").build())

            .build();

        final Response responseNotificationLetter = notificationsTestService.postLetterNotification(
            userTokenPaymentRefundApprover ,
            serviceTokenPayBubble ,
            testConfigProperties.baseTestUrl ,
            refundNotificationLetterRequest
        );
        assertThat(responseNotificationLetter.getStatusCode()).isEqualTo(HttpStatus.CREATED.value());
        deleteNotifications(reference);
    }

    @Test
    public void negativeIncorrectEmailFormatSendEmailNotificationRequest() {

        String reference = "Functional Test";
        RefundNotificationEmailRequest refundNotificationEmailRequest = RefundNotificationEmailRequest.refundNotificationEmailRequestWith()
            .templateId(emailTemplateId)
            .recipientEmailAddress("testtestcom")
            .reference(reference)
            .emailReplyToId(emailReplyToId)
            .notificationType(NotificationType.EMAIL)
            .personalisation(Personalisation.personalisationRequestWith().ccdCaseNumber(CCD_CASE_NUMBER).refundReference("RF-1234-1234-1234-1234").refundAmount(
                BigDecimal.valueOf(10)).refundReason("RR001").build())

            .build();

        final Response responseNotificationEmail = notificationsTestService.postEmailNotification(
            userTokenPaymentRefundApprover ,
            serviceTokenPayBubble ,
            testConfigProperties.baseTestUrl ,
            refundNotificationEmailRequest
        );
        assertThat(responseNotificationEmail.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    public void negativeInvalidPostcodeSendLetterNotificationRequest() {

        String reference = "Functional Test";
        RefundNotificationLetterRequest refundNotificationLetterRequest = RefundNotificationLetterRequest.refundNotificationLetterRequestWith()
            .templateId(letterTemplateId)
            .recipientPostalAddress(RecipientPostalAddress.recipientPostalAddressWith()
                                        .addressLine(" 1 Test Street")
                                        .city(CITY)
                                        .county(COUNTY)
                                        .country("England")
                                        .postalCode("SSGSSB")
                                        .build())
            .reference(reference)
            .notificationType(NotificationType.LETTER)
            .serviceName("Probate")
            .personalisation(Personalisation.personalisationRequestWith().ccdCaseNumber(CCD_CASE_NUMBER).refundReference("RF-1234-1234-1234-1234").refundAmount(
                BigDecimal.valueOf(10)).refundReason("RR001").build())

            .build();

        final Response responseNotificationLetter = notificationsTestService.postLetterNotification(
            userTokenPaymentRefundApprover ,
            serviceTokenPayBubble ,
            testConfigProperties.baseTestUrl ,
            refundNotificationLetterRequest
        );

        assertThat(responseNotificationLetter.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY.value());
    }

    @Test
    public void getDetailsForSentEmailNotification(){

        String reference = "RF-1234-" + RandomUtils.nextInt();
        RefundNotificationEmailRequest refundNotificationEmailRequest = RefundNotificationEmailRequest.refundNotificationEmailRequestWith()
            .templateId(emailTemplateId)
            .recipientEmailAddress("vat12@mailinator.com")
            .reference(reference)
            .emailReplyToId(emailReplyToId)
            .notificationType(NotificationType.EMAIL)
            .serviceName("Probate")
            .personalisation(Personalisation.personalisationRequestWith().ccdCaseNumber(CCD_CASE_NUMBER).refundReference(reference).refundAmount(
                BigDecimal.valueOf(10)).refundReason("RR001").build())

            .build();

        final Response responseNotificationEmail = notificationsTestService.postEmailNotification(
            userTokenPaymentRefundApprover ,
            serviceTokenPayBubble ,
            testConfigProperties.baseTestUrl ,
            refundNotificationEmailRequest
        );
        assertThat(responseNotificationEmail.getStatusCode()).isEqualTo(HttpStatus.CREATED.value());

        final Response responseNotification = notificationsTestService.getNotification(
            userTokenPaymentRefundApprover ,
            serviceTokenPayBubble ,
            testConfigProperties.baseTestUrl ,
            reference
        );

        assertThat(responseNotification.getStatusCode()).isEqualTo(HttpStatus.OK.value());

        List<Map<String,String>> getNotificationsResponse =  responseNotification.getBody().jsonPath().getList("notifications");
        assertThat(getNotificationsResponse.size()).isGreaterThanOrEqualTo(1);

        deleteNotifications(reference);
    }

    @Test
    public void getNotificationDetailsForEmptyReference() {
        final Response responseNotification = notificationsTestService.getNotification(
            userTokenPaymentRefundApprover,
            serviceTokenPayBubble,
            testConfigProperties.baseTestUrl,
            ""
        );

        assertThat(responseNotification.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @Test
    public void letterNotificationTemplateForSendRefund() {

        DocPreviewRequest request = DocPreviewRequest.docPreviewRequestWith()
            .notificationType(NotificationType.LETTER)
            .recipientEmailAddress("test@hmcts.net")
            .serviceName("Probate")
            .personalisation(
                Personalisation.personalisationRequestWith().ccdCaseNumber(CCD_CASE_NUMBER).refundAmount(
                    BigDecimal.valueOf(10)).refundReason("RR001").refundReference("RF-1234-1234-1234-1234").build())
            .paymentChannel("telephony")
            .paymentMethod("card")
            .recipientPostalAddress(RecipientPostalAddress.recipientPostalAddressWith().addressLine("abc").postalCode("123 456")
                                        .county("london").country("UK").city("london").build())
            .build();

        final Response responseNotificationLetter = notificationsTestService.getTemplateNotificationPreview(
            userTokenPaymentRefundApprover ,
            serviceTokenPayBubble ,
            testConfigProperties.baseTestUrl ,
            request
        );

        NotificationTemplatePreviewResponse notificationTemplatePreviewResponse = responseNotificationLetter.getBody().as(NotificationTemplatePreviewResponse.class);
        assertThat(notificationTemplatePreviewResponse.getTemplateType().equals("letter"));
        assertThat(notificationTemplatePreviewResponse.getTemplateId().equals(cardPbaLetterTemplateId));
    }

    @Test
    public void letterNotificationTemplateForRefundWhenContacted() {

        DocPreviewRequest request = DocPreviewRequest.docPreviewRequestWith()
            .notificationType(NotificationType.LETTER)
            .recipientEmailAddress("test@hmcts.net")
            .serviceName("Probate")
            .personalisation(
                Personalisation.personalisationRequestWith().ccdCaseNumber(CCD_CASE_NUMBER).refundAmount(
                    BigDecimal.valueOf(10)).refundReason("RR001").refundReference("RF-1234-1234-1234-1234").build())
            .paymentChannel("bull scan")
            .paymentMethod("cash")
            .recipientPostalAddress(RecipientPostalAddress.recipientPostalAddressWith().addressLine("abc").postalCode("123 456")
                                        .county("london").country("UK").city("london").build())
            .build();

        final Response responseNotificationLetter = notificationsTestService.getTemplateNotificationPreview(
            userTokenPaymentRefundApprover ,
            serviceTokenPayBubble ,
            testConfigProperties.baseTestUrl ,
            request
        );

        NotificationTemplatePreviewResponse notificationTemplatePreviewResponse = responseNotificationLetter.getBody().as(NotificationTemplatePreviewResponse.class);
        assertThat(notificationTemplatePreviewResponse.getTemplateType().equals("letter"));
        assertThat(notificationTemplatePreviewResponse.getTemplateId().equals(chequePoCashLetterTemplateId));
    }

    @Test
    public void emailNotificationTemplateForSendRefund() {

        DocPreviewRequest request = DocPreviewRequest.docPreviewRequestWith()
            .notificationType(NotificationType.EMAIL)
            .recipientEmailAddress("test@hmcts.net")
            .serviceName("Probate")
            .personalisation(
                Personalisation.personalisationRequestWith().ccdCaseNumber(CCD_CASE_NUMBER).refundAmount(
                    BigDecimal.valueOf(10)).refundReason("RR001").refundReference("RF-1234-1234-1234-1234").build())
            .paymentChannel("online")
            .paymentMethod("card")
            .build();

        final Response responseNotificationLetter = notificationsTestService.getTemplateNotificationPreview(
            userTokenPaymentRefundApprover ,
            serviceTokenPayBubble ,
            testConfigProperties.baseTestUrl ,
            request
        );

        NotificationTemplatePreviewResponse notificationTemplatePreviewResponse = responseNotificationLetter.getBody().as(NotificationTemplatePreviewResponse.class);
        assertThat(notificationTemplatePreviewResponse.getTemplateType().equals("email"));
        assertThat(notificationTemplatePreviewResponse.getTemplateId().equals(cardPbaEmailTemplateId));
    }

    @Test
    public void emailNotificationTemplateForRefundWhenContacted() {

        DocPreviewRequest request = DocPreviewRequest.docPreviewRequestWith()
            .notificationType(NotificationType.EMAIL)
            .recipientEmailAddress("test@hmcts.net")
            .serviceName("Probate")
            .personalisation(
                Personalisation.personalisationRequestWith().ccdCaseNumber(CCD_CASE_NUMBER).refundAmount(
                    BigDecimal.valueOf(10)).refundReason("RR001").refundReference("RF-1234-1234-1234-1234").build())
            .paymentChannel("bulk scan")
            .paymentMethod("cash")
            .build();

        final Response responseNotificationLetter = notificationsTestService.getTemplateNotificationPreview(
            userTokenPaymentRefundApprover ,
            serviceTokenPayBubble ,
            testConfigProperties.baseTestUrl ,
            request
        );

        NotificationTemplatePreviewResponse notificationTemplatePreviewResponse = responseNotificationLetter.getBody().as(NotificationTemplatePreviewResponse.class);
        assertThat(notificationTemplatePreviewResponse.getTemplateType().equals("email"));
        assertThat(notificationTemplatePreviewResponse.getTemplateId().equals(chequePoCashEmailTemplateId));
    }

    @Test
    public void sendEmailNotificationRequestWithTemplatePreviews() {

        String reference = "FunctionalTest1";
        RefundNotificationEmailRequest refundNotificationEmailRequest = RefundNotificationEmailRequest.refundNotificationEmailRequestWith()
            .templateId(emailTemplateId)
            .recipientEmailAddress("vat12@mailinator.com")
            .reference(reference)
            .emailReplyToId(emailReplyToId)
            .notificationType(NotificationType.EMAIL)
            .serviceName("Probate")
            .personalisation(Personalisation.personalisationRequestWith().ccdCaseNumber(CCD_CASE_NUMBER).refundReference(reference).refundAmount(
                BigDecimal.valueOf(10)).refundReason("RR001").build())
            .templatePreview(TemplatePreviewDto.templatePreviewDtoWith().id(UUID.randomUUID())
                                 .templateType("email")
                                 .version(11)
                                 .body("Dear Sir Madam")
                                 .subject("HMCTS refund request approved")
                                 .html("Dear Sir Madam")
                                 .from(FromTemplateContact.buildFromTemplateContactWith()
                                           .fromEmailAddress("test@test.com").build())
                                 .build())
            .build();

        final Response responseNotificationEmail = notificationsTestService.postEmailNotification(
            userTokenPaymentRefundApprover ,
            serviceTokenPayBubble ,
            testConfigProperties.baseTestUrl,
            refundNotificationEmailRequest
        );
        assertThat(responseNotificationEmail.getStatusCode()).isEqualTo(HttpStatus.CREATED.value());
        assertThat("Notification sent successfully via email")
            .isEqualTo(responseNotificationEmail.body().asString());

        deleteNotifications(reference);
    }

    @Test
    public void sendLetterNotificationRequestWithTemplatePreview() {

        String reference ="FunctionalTest2";
        RefundNotificationLetterRequest refundNotificationLetterRequest = RefundNotificationLetterRequest.refundNotificationLetterRequestWith()
            .templateId(letterTemplateId)
            .recipientPostalAddress(RecipientPostalAddress.recipientPostalAddressWith()
                                        .addressLine("102 Petty France")
                                        .city(CITY)
                                        .county(COUNTY)
                                        .country("England")
                                        .postalCode("SW1H 9AJ")
                                        .build())
            .reference(reference)
            .notificationType(NotificationType.LETTER)
            .serviceName("Probate")
            .personalisation(Personalisation.personalisationRequestWith().ccdCaseNumber(CCD_CASE_NUMBER).refundReference(reference).refundAmount(
                BigDecimal.valueOf(10)).refundReason("RR001").build())
            .templatePreview(TemplatePreviewDto.templatePreviewDtoWith().id(UUID.randomUUID())
                                 .templateType("email")
                                 .version(11)
                                 .body("Dear Sir Madam")
                                 .subject("HMCTS refund request approved")
                                 .html("Dear Sir Madam")
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

        final Response responseNotificationLetter = notificationsTestService.postLetterNotification(
            userTokenPaymentRefundApprover ,
            serviceTokenPayBubble ,
            testConfigProperties.baseTestUrl ,
            refundNotificationLetterRequest
        );
        assertThat(responseNotificationLetter.getStatusCode()).isEqualTo(HttpStatus.CREATED.value());
        assertThat("Notification sent successfully via letter")
            .isEqualTo(responseNotificationLetter.body().asString());

        deleteNotifications(reference);
    }

    @Test
    public void sendEmailNotificationRequestWithRefundWhenContacted() {

        String reference = "RF-1234-" + RandomUtils.nextInt();
        RefundNotificationEmailRequest refundNotificationEmailRequest = RefundNotificationEmailRequest.refundNotificationEmailRequestWith()
            .templateId(chequePoCashEmailTemplateId)
            .reference(reference)
            .notificationType(NotificationType.EMAIL)
            .emailReplyToId(emailReplyToId)
            .serviceName("Probate")
            .recipientEmailAddress("vat12@mailinator.com")
            .personalisation(Personalisation.personalisationRequestWith()
                                 .ccdCaseNumber(CCD_CASE_NUMBER)
                                 .refundReference(reference)
                                 .refundAmount(BigDecimal.valueOf(10))
                                 .refundReason("RR001")
                                 .build())
            .build();

        final Response responseNotificationEmail = notificationsTestService.postEmailNotification(
            userTokenPaymentRefundApprover ,
            serviceTokenPayBubble ,
            testConfigProperties.baseTestUrl,
            refundNotificationEmailRequest
        );
        assertThat(responseNotificationEmail.getStatusCode()).isEqualTo(HttpStatus.CREATED.value());

        final Response responseNotification = notificationsTestService.getNotification(
            userTokenPaymentRefundApprover ,
            serviceTokenPayBubble ,
            testConfigProperties.baseTestUrl ,
            reference
        );

        assertThat(responseNotification.getStatusCode()).isEqualTo(HttpStatus.OK.value());

        List<Map> notificationList =  responseNotification.getBody().jsonPath().getList("notifications");
        assertThat(notificationList.size()).isGreaterThanOrEqualTo(1);
        Map contactDetails = (Map) notificationList.get(0).get("contact_details");
        assertThat(contactDetails.get("email")).isEqualTo("vat12@mailinator.com");
        Map sendNotification = (Map) notificationList.get(0).get("sent_notification");
        assertThat(sendNotification.get("template_id")).isEqualTo(chequePoCashEmailTemplateId);

        deleteNotifications(reference);
    }

    @Test
    public void sendEmailNotificationRequestWithSendRefund() {

        String reference = "RF-1234-" + RandomUtils.nextInt();
        RefundNotificationEmailRequest refundNotificationEmailRequest = RefundNotificationEmailRequest.refundNotificationEmailRequestWith()
            .templateId(cardPbaEmailTemplateId)
            .reference(reference)
            .notificationType(NotificationType.EMAIL)
            .serviceName("Probate")
            .emailReplyToId(emailReplyToId)
            .recipientEmailAddress("vat12@mailinator.com")
            .personalisation(Personalisation.personalisationRequestWith()
                                 .ccdCaseNumber(CCD_CASE_NUMBER)
                                 .refundReference(reference)
                                 .refundAmount(BigDecimal.valueOf(10))
                                 .refundReason("RR001")
                                 .build())
            .build();

        final Response responseNotificationEmail = notificationsTestService.postEmailNotification(
            userTokenPaymentRefundApprover ,
            serviceTokenPayBubble ,
            testConfigProperties.baseTestUrl,
            refundNotificationEmailRequest
        );
        assertThat(responseNotificationEmail.getStatusCode()).isEqualTo(HttpStatus.CREATED.value());

        final Response responseNotification = notificationsTestService.getNotification(
            userTokenPaymentRefundApprover ,
            serviceTokenPayBubble ,
            testConfigProperties.baseTestUrl ,
            reference
        );

        assertThat(responseNotification.getStatusCode()).isEqualTo(HttpStatus.OK.value());

        List<Map> notificationList =  responseNotification.getBody().jsonPath().getList("notifications");
        assertThat(notificationList.size()).isGreaterThanOrEqualTo(1);
        Map contactDetails = (Map) notificationList.get(0).get("contact_details");
        assertThat(contactDetails.get("email")).isEqualTo("vat12@mailinator.com");
        Map sendNotification = (Map) notificationList.get(0).get("sent_notification");
        assertThat(sendNotification.get("template_id")).isEqualTo(cardPbaEmailTemplateId);

        deleteNotifications(reference);
    }

    @Test
    public void sendLetterNotificationRequestSendRefund() {
        String reference = "RF-1234-" + RandomUtils.nextInt();
        RefundNotificationLetterRequest refundNotificationLetterRequest = RefundNotificationLetterRequest.refundNotificationLetterRequestWith()
            .templateId(cardPbaLetterTemplateId)
            .recipientPostalAddress(RecipientPostalAddress.recipientPostalAddressWith()
                                        .addressLine("102 Petty France")
                                        .city(CITY)
                                        .county(COUNTY)
                                        .country("England")
                                        .postalCode("SW1H 9AJ")
                                        .build())
            .reference(reference)
            .notificationType(NotificationType.LETTER)
            .serviceName("Probate")
            .personalisation(Personalisation.personalisationRequestWith().ccdCaseNumber(CCD_CASE_NUMBER).refundReference(reference).refundAmount(
                BigDecimal.valueOf(10)).refundReason("RR001").build())

            .build();

        final Response responseNotificationLetter = notificationsTestService.postLetterNotification(
            userTokenPaymentRefundApprover ,
            serviceTokenPayBubble ,
            testConfigProperties.baseTestUrl ,
            refundNotificationLetterRequest
        );
        assertThat(responseNotificationLetter.getStatusCode()).isEqualTo(HttpStatus.CREATED.value());

        final Response responseNotification = notificationsTestService.getNotification(
            userTokenPaymentRefundApprover ,
            serviceTokenPayBubble ,
            testConfigProperties.baseTestUrl ,
            reference
        );

        assertThat(responseNotification.getStatusCode()).isEqualTo(HttpStatus.OK.value());

        List<Map> notificationList =  responseNotification.getBody().jsonPath().getList("notifications");
        assertThat(notificationList.size()).isGreaterThanOrEqualTo(1);
        Map sendNotification = (Map) notificationList.get(0).get("sent_notification");
        assertThat(sendNotification.get("template_id")).isEqualTo(cardPbaLetterTemplateId);

        deleteNotifications(reference);
    }

    @Test
    public void sendLetterNotificationRequestRefundWhenContacted() {
        String reference = "RF-1234-" + RandomUtils.nextInt();
        RefundNotificationLetterRequest refundNotificationLetterRequest = RefundNotificationLetterRequest.refundNotificationLetterRequestWith()
            .templateId(chequePoCashLetterTemplateId)
            .recipientPostalAddress(RecipientPostalAddress.recipientPostalAddressWith()
                                        .addressLine("102 Petty France")
                                        .city(CITY)
                                        .county(COUNTY)
                                        .country("England")
                                        .postalCode("SW1H 9AJ")
                                        .build())
            .reference(reference)
            .notificationType(NotificationType.LETTER)
            .serviceName("Probate")
            .personalisation(Personalisation.personalisationRequestWith().ccdCaseNumber(CCD_CASE_NUMBER).refundReference(reference).refundAmount(
                BigDecimal.valueOf(10)).refundReason("RR001").build())

            .build();

        final Response responseNotificationLetter = notificationsTestService.postLetterNotification(
            userTokenPaymentRefundApprover ,
            serviceTokenPayBubble ,
            testConfigProperties.baseTestUrl ,
            refundNotificationLetterRequest
        );
        assertThat(responseNotificationLetter.getStatusCode()).isEqualTo(HttpStatus.CREATED.value());

        final Response responseNotification = notificationsTestService.getNotification(
            userTokenPaymentRefundApprover ,
            serviceTokenPayBubble ,
            testConfigProperties.baseTestUrl ,
            reference
        );

        assertThat(responseNotification.getStatusCode()).isEqualTo(HttpStatus.OK.value());

        List<Map> notificationList =  responseNotification.getBody().jsonPath().getList("notifications");
        assertThat(notificationList.size()).isGreaterThanOrEqualTo(1);
        Map sendNotification = (Map) notificationList.get(0).get("sent_notification");
        assertThat(sendNotification.get("template_id")).isEqualTo(chequePoCashLetterTemplateId);

        deleteNotifications(reference);
    }

    @Test
    public void sendEmailNotificationRequestForFromEmailAddress() {

        String reference = "FunctionalTest1";
        RefundNotificationEmailRequest refundNotificationEmailRequest = RefundNotificationEmailRequest.refundNotificationEmailRequestWith()
            .templateId(emailTemplateId)
            .recipientEmailAddress("vat12@mailinator.com")
            .reference(reference)
            .emailReplyToId(emailReplyToId)
            .notificationType(NotificationType.EMAIL)
            .serviceName("Probate")
            .personalisation(Personalisation.personalisationRequestWith().ccdCaseNumber(CCD_CASE_NUMBER).refundReference(reference).refundAmount(
                BigDecimal.valueOf(10)).refundReason("RR001").build())
            .build();

        final Response responseNotificationEmail = notificationsTestService.postEmailNotification(
            userTokenPaymentRefundApprover ,
            serviceTokenPayBubble ,
            testConfigProperties.baseTestUrl,
            refundNotificationEmailRequest
        );
        assertThat(responseNotificationEmail.getStatusCode()).isEqualTo(HttpStatus.CREATED.value());

        final Response responseNotification = notificationsTestService.getNotification(
            userTokenPaymentRefundApprover ,
            serviceTokenPayBubble ,
            testConfigProperties.baseTestUrl ,
            reference
        );

        assertThat(responseNotification.getStatusCode()).isEqualTo(HttpStatus.OK.value());

        List<Map> notificationList =  responseNotification.getBody().jsonPath().getList("notifications");
        assertThat(notificationList.size()).isGreaterThanOrEqualTo(1);
        Map contactDetails = (Map) notificationList.get(0).get("contact_details");
        assertThat(contactDetails.get("email")).isEqualTo("vat12@mailinator.com");
        assertThat(((HashMap)((HashMap)notificationList.get(0).get("sent_notification")).get("from")).get("from_email_address"))
            .isEqualTo("contactprobate@justice.gov.uk");

        deleteNotifications(reference);
    }

    @Test
    public void sendEmailNotificationRequestForFromEmailAddressThrows5XXError() {

        String reference = "FunctionalTest1";
        RefundNotificationEmailRequest refundNotificationEmailRequest = RefundNotificationEmailRequest.refundNotificationEmailRequestWith()
            .templateId(emailTemplateId)
            .recipientEmailAddress("vat12@mailinator.com")
            .reference(reference)
            .emailReplyToId(emailReplyToId)
            .notificationType(NotificationType.EMAIL)
            .serviceName("Wrong Service")
            .personalisation(Personalisation.personalisationRequestWith().ccdCaseNumber(CCD_CASE_NUMBER).refundReference(reference).refundAmount(
                BigDecimal.valueOf(10)).refundReason("RR001").build())
            .build();

        final Response responseNotificationEmail = notificationsTestService.postEmailNotification(
            userTokenPaymentRefundApprover ,
            serviceTokenPayBubble ,
            testConfigProperties.baseTestUrl,
            refundNotificationEmailRequest
        );
        assertThat(500).isEqualTo(responseNotificationEmail.getStatusCode());
    }

    @Test
    public void sendLetterNotificationRequestForFromMailAddress() {

        String reference = "FunctionalTest2";
        RefundNotificationLetterRequest refundNotificationLetterRequest = RefundNotificationLetterRequest.refundNotificationLetterRequestWith()
            .templateId(letterTemplateId)
            .recipientPostalAddress(RecipientPostalAddress.recipientPostalAddressWith()
                                        .addressLine("102 Petty France")
                                        .city(CITY)
                                        .county(COUNTY)
                                        .country("England")
                                        .postalCode("SW1H 9AJ")
                                        .build())
            .reference(reference)
            .notificationType(NotificationType.LETTER)
            .serviceName("Probate")
            .personalisation(Personalisation.personalisationRequestWith().ccdCaseNumber(CCD_CASE_NUMBER).refundReference(reference).refundAmount(
                BigDecimal.valueOf(10)).refundReason("RR001").build())

            .build();

        final Response responseNotificationLetter = notificationsTestService.postLetterNotification(
            userTokenPaymentRefundApprover ,
            serviceTokenPayBubble ,
            testConfigProperties.baseTestUrl ,
            refundNotificationLetterRequest
        );
        assertThat(responseNotificationLetter.getStatusCode()).isEqualTo(HttpStatus.CREATED.value());

        final Response responseNotification = notificationsTestService.getNotification(
            userTokenPaymentRefundApprover ,
            serviceTokenPayBubble ,
            testConfigProperties.baseTestUrl ,
            reference
        );

        assertThat(responseNotification.getStatusCode()).isEqualTo(HttpStatus.OK.value());

        List<Map> notificationList =  responseNotification.getBody().jsonPath().getList("notifications");
        assertThat(notificationList.size()).isGreaterThanOrEqualTo(1);
        Map contactDetails = (Map) notificationList.get(0).get("contact_details");
        assertThat(contactDetails.get("postal_code")).isEqualTo("SW1H 9AJ");

        deleteNotifications(reference);
    }

    @Test
    public void emailNotificationTemplateForSendRefundWhenReasonIsAmendedClaim() {

        DocPreviewRequest request = DocPreviewRequest.docPreviewRequestWith()
            .notificationType(NotificationType.EMAIL)
            .recipientEmailAddress("test@hmcts.net")
            .serviceName("Probate")
            .personalisation(
                Personalisation.personalisationRequestWith().ccdCaseNumber(CCD_CASE_NUMBER).refundAmount(
                    BigDecimal.valueOf(10)).refundReason("RR001").refundReference("RF-1234-1234-1234-1234").build())
            .paymentChannel("online")
            .paymentMethod("card")
            .build();

        final Response responseNotificationLetter = notificationsTestService.getTemplateNotificationPreview(
            userTokenPaymentRefundApprover ,
            serviceTokenPayBubble ,
            testConfigProperties.baseTestUrl ,
            request
        );

        NotificationTemplatePreviewResponse notificationTemplatePreviewResponse = responseNotificationLetter.getBody().as(NotificationTemplatePreviewResponse.class);
        assertThat(notificationTemplatePreviewResponse.getTemplateType().equals("email"));
        assertThat(notificationTemplatePreviewResponse.getHtml().contains("There has been an amendment to your claim"));
    }

    @Test
    public void letterNotificationTemplateForSendRefundReasonIsApplicationRejected() {

        DocPreviewRequest request = DocPreviewRequest.docPreviewRequestWith()
            .notificationType(NotificationType.LETTER)
            .recipientEmailAddress("test@hmcts.net")
            .serviceName("Probate")
            .personalisation(
                Personalisation.personalisationRequestWith().ccdCaseNumber(CCD_CASE_NUMBER).refundAmount(
                    BigDecimal.valueOf(10)).refundReason("RR003").refundReference("RF-1234-1234-1234-1234").build())
            .paymentChannel("telephony")
            .paymentMethod("card")
            .recipientPostalAddress(RecipientPostalAddress.recipientPostalAddressWith().addressLine("abc").postalCode("123 456")
                                        .county("london").country("UK").city("london").build())
            .build();

        final Response responseNotificationLetter = notificationsTestService.getTemplateNotificationPreview(
            userTokenPaymentRefundApprover ,
            serviceTokenPayBubble ,
            testConfigProperties.baseTestUrl ,
            request
        );

        NotificationTemplatePreviewResponse notificationTemplatePreviewResponse = responseNotificationLetter.getBody().as(NotificationTemplatePreviewResponse.class);
        assertThat(notificationTemplatePreviewResponse.getTemplateType().equals("letter"));
        assertThat(notificationTemplatePreviewResponse.getBody().contains("Application fee has been refunded due to non-processing of application"));

    }

    @Test
    public void sendLetterNotificationRequestRefundWhenContactedWhenReasonIsCourtDiscretion() {
        String reference = "RF-1234-" + RandomUtils.nextInt();
        RefundNotificationLetterRequest refundNotificationLetterRequest = RefundNotificationLetterRequest.refundNotificationLetterRequestWith()
            .templateId(chequePoCashLetterTemplateId)
            .recipientPostalAddress(RecipientPostalAddress.recipientPostalAddressWith()
                                        .addressLine("102 Petty France")
                                        .city(CITY)
                                        .county(COUNTY)
                                        .country("England")
                                        .postalCode("SW1H 9AJ")
                                        .build())
            .reference(reference)
            .notificationType(NotificationType.LETTER)
            .serviceName("Probate")
            .personalisation(Personalisation.personalisationRequestWith().ccdCaseNumber(CCD_CASE_NUMBER)
                                 .refundReference(reference).refundAmount(
                    BigDecimal.valueOf(10)).refundReason("RR007").build())

            .build();

        final Response responseNotificationLetter = notificationsTestService.postLetterNotification(
            userTokenPaymentRefundApprover,
            serviceTokenPayBubble,
            testConfigProperties.baseTestUrl,
            refundNotificationLetterRequest
        );
        assertThat(responseNotificationLetter.getStatusCode()).isEqualTo(HttpStatus.CREATED.value());

        final Response responseNotification = notificationsTestService.getNotification(
            userTokenPaymentRefundApprover,
            serviceTokenPayBubble,
            testConfigProperties.baseTestUrl,
            reference
        );

        assertThat(responseNotification.getStatusCode()).isEqualTo(HttpStatus.OK.value());

        List<Map> notificationList = responseNotification.getBody().jsonPath().getList("notifications");
        assertThat(notificationList.size()).isGreaterThanOrEqualTo(1);
        Map sendNotification = (Map) notificationList.get(0).get("sent_notification");

        String bodyString = sendNotification.get("body").toString();
        assertThat(bodyString.contains("Due to court's discretion a refund has been approved"));

        deleteNotifications(reference);
    }

    @Test
    public void sendEmailNotificationRequestWithSendRefundWhenReasonIsDuplicateFee() {

        String reference = "RF-1234-" + RandomUtils.nextInt();
        RefundNotificationEmailRequest refundNotificationEmailRequest = RefundNotificationEmailRequest.refundNotificationEmailRequestWith()
            .templateId(cardPbaEmailTemplateId)
            .reference(reference)
            .notificationType(NotificationType.EMAIL)
            .serviceName("Probate")
            .emailReplyToId(emailReplyToId)
            .recipientEmailAddress("vat12@mailinator.com")
            .personalisation(Personalisation.personalisationRequestWith()
                                 .ccdCaseNumber(CCD_CASE_NUMBER)
                                 .refundReference(reference)
                                 .refundAmount(BigDecimal.valueOf(10))
                                 .refundReason("RR009")
                                 .build())
            .build();

        final Response responseNotificationEmail = notificationsTestService.postEmailNotification(
            userTokenPaymentRefundApprover ,
            serviceTokenPayBubble ,
            testConfigProperties.baseTestUrl,
            refundNotificationEmailRequest
        );
        assertThat(responseNotificationEmail.getStatusCode()).isEqualTo(HttpStatus.CREATED.value());

        final Response responseNotification = notificationsTestService.getNotification(
            userTokenPaymentRefundApprover ,
            serviceTokenPayBubble ,
            testConfigProperties.baseTestUrl ,
            reference
        );

        assertThat(responseNotification.getStatusCode()).isEqualTo(HttpStatus.OK.value());

        List<Map> notificationList =  responseNotification.getBody().jsonPath().getList("notifications");
        assertThat(notificationList.size()).isGreaterThanOrEqualTo(1);
        Map contactDetails = (Map) notificationList.get(0).get("contact_details");
        assertThat(contactDetails.get("email")).isEqualTo("vat12@mailinator.com");
        Map sendNotification = (Map) notificationList.get(0).get("sent_notification");
        String bodyString = sendNotification.get("html").toString();
        assertThat(bodyString.contains("A duplicate fee was processed and has now been refunded"));

        deleteNotifications(reference);
    }

    @Test
    public void returnAddressWhenValidPostCodeProvided() {

        String postCode ="SW1H 9AJ";
        final Response responsePostCodeLookUp = notificationsTestService.getPostCodeLookup(
            userTokenPaymentRefundApprover ,
            serviceTokenPayBubble ,
            testConfigProperties.baseTestUrl ,
            postCode
        );

        assertThat(responsePostCodeLookUp.getStatusCode()).isEqualTo(HttpStatus.OK.value());

        List<Map> addressList =  responsePostCodeLookUp.getBody().jsonPath().getList("results");
        Map results = (Map) addressList.get(0).get("DPA");
        assertThat(results.get("ADDRESS")).isEqualTo("MINISTRY OF JUSTICE, SEVENTH FLOOR, 102, PETTY FRANCE, LONDON, SW1H 9AJ");
        assertThat(results.get("POSTCODE")).isEqualTo("SW1H 9AJ");
        assertThat(results.get("POST_TOWN")).isEqualTo("LONDON");

    }

    @Test
    public void sendLetterNotificationRequestForFromMailAddressThrows5XXError() {

        String reference = "FunctionalTest2";
        RefundNotificationLetterRequest refundNotificationLetterRequest = RefundNotificationLetterRequest.refundNotificationLetterRequestWith()
            .templateId(letterTemplateId)
            .recipientPostalAddress(RecipientPostalAddress.recipientPostalAddressWith()
                                        .addressLine("102 Petty France")
                                        .city(CITY)
                                        .county(COUNTY)
                                        .country("England")
                                        .postalCode("SW1H 9AJ")
                                        .build())
            .reference(reference)
            .notificationType(NotificationType.LETTER)
            .serviceName("Wrong Service")
            .personalisation(Personalisation.personalisationRequestWith().ccdCaseNumber(CCD_CASE_NUMBER).refundReference("RF-1234-1234-1234-1234").refundAmount(
                BigDecimal.valueOf(10)).refundReason("RR001").build())

            .build();

        final Response responseNotificationLetter = notificationsTestService.postLetterNotification(
            userTokenPaymentRefundApprover ,
            serviceTokenPayBubble ,
            testConfigProperties.baseTestUrl ,
            refundNotificationLetterRequest
        );

        assertThat(500).isEqualTo(responseNotificationLetter.getStatusCode());
    }

    private void deleteNotifications(String reference) {
        // delete notification record
        notificationsTestService.deleteNotification(userTokenPaymentRefundApprover, serviceTokenPayBubble,
                                                    testConfigProperties.baseTestUrl, reference)
            .then().statusCode(NO_CONTENT.value());
    }
}
