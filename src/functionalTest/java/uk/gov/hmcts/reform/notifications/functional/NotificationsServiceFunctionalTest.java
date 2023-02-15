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
        deleteNotifications(reference);
    }

    private void deleteNotifications(String reference) {
        // delete notification record
        notificationsTestService.deleteNotification(userTokenPaymentRefundApprover, serviceTokenPayBubble,
                                                    testConfigProperties.baseTestUrl, reference)
            .then().statusCode(NO_CONTENT.value());
    }
}
