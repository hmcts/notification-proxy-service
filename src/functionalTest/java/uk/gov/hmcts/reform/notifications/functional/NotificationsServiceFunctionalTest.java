package uk.gov.hmcts.reform.notifications.functional;

import org.junit.Ignore;
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
import uk.gov.hmcts.reform.notifications.dtos.request.Personalisation;
import uk.gov.hmcts.reform.notifications.dtos.request.RecipientPostalAddress;
import uk.gov.hmcts.reform.notifications.dtos.request.RefundNotificationEmailRequest;
import uk.gov.hmcts.reform.notifications.dtos.request.RefundNotificationLetterRequest;
import uk.gov.hmcts.reform.notifications.functional.config.IdamService;
import uk.gov.hmcts.reform.notifications.functional.config.NotificationsTestService;
import uk.gov.hmcts.reform.notifications.functional.config.S2sTokenService;
import uk.gov.hmcts.reform.notifications.functional.config.TestConfigProperties;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

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

    @Autowired
    private TestConfigProperties testConfigProperties;

    @Autowired
    private IdamService idamService;

    @Autowired
    private S2sTokenService s2sTokenService;

    @Autowired
    private NotificationsTestService notificationsTestServicel;

    private String serviceTokenPayBubble;

    private String userTokenPaymentRefundApprover;

    private boolean isTokensInitialized;

    private static final String REFERENCE = "RF-1111-2222-3333-4444";

    private static final String CCD_CASE_NUMBER = "1234567890123456";

    private static final String CITY = "London";

    private static final String COUNTY = "London";

    @BeforeAll
    public void setUp() {

        if (!isTokensInitialized) {

            userTokenPaymentRefundApprover =
                idamService.createUserWithSearchScope("idam.user.ccpayrefundsapi@hmcts.net").getAuthorisationToken();

            //serviceTokenPayBubble =
              //  s2sTokenService.getS2sToken("refunds_api", testConfigProperties.s2sRefundsApi);

            isTokensInitialized = true;

        }
    }

    @Test
    public void testSimple() {
        Object obj = new Object();
        assertThat(obj).isNotNull();
    }


}
