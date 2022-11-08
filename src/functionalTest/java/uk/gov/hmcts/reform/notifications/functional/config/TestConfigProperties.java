package uk.gov.hmcts.reform.notifications.functional.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
public class TestConfigProperties {

    @Autowired
    public Oauth2 oauth2;

    @Value("${test.url.notification}")
    public String baseTestUrl;

    @Value("${generated.user.email.pattern}")
    public String generatedUserEmailPattern;

    @Value("${test.user.password}")
    public String testUserPassword;
    @Value("${s2s.service.refunds.secret}")
    public String s2sRefundsApi;

    @Value("${idam.api.url}")
    public String idamApiUrl;

    @Value("${s2s.url}")
    private String s2sBaseUrl;

    @Value("${s2s.service.paymentapp.secret}")
    public String s2sServiceSecret;

    @Value("${payments.account.existing.account.number}")
    public String existingAccountNumber;

    @Value("${payments.account.fake.account.number}")
    public String fakeAccountNumber;

    @Value("${idam.paybubble.client.id}")
    public String idamPayBubbleClientID;

    @Value("${idam.paybubble.client.secret}")
    public String idamPayBubbleClientSecret;


}

