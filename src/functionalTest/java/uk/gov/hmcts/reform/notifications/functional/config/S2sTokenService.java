package uk.gov.hmcts.reform.notifications.functional.config;


import feign.Feign;
import feign.jackson.JacksonEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class S2sTokenService {

    private final TestConfigProperties testProps;



    private final OneTimePasswordFactory oneTimePasswordFactory;
    private final S2sApi s2sApi;
    private static final Logger LOG = LoggerFactory.getLogger(S2sTokenService.class);

    @Autowired
    public S2sTokenService(OneTimePasswordFactory oneTimePasswordFactory, TestConfigProperties testProps) {
        this.oneTimePasswordFactory = oneTimePasswordFactory;
        this.testProps = testProps;
        s2sApi = Feign.builder()
            .encoder(new JacksonEncoder())
            .target(S2sApi.class, testProps.getS2sBaseUrl());
    }

    public String getS2sToken(String microservice, String secret) {
        String otp = oneTimePasswordFactory.validOneTimePassword(secret);
        LOG.info("s2sApi : " + s2sApi.toString());
        LOG.info("testProps : " + testProps.getS2sBaseUrl());
        LOG.info("microservice : " + microservice);
        LOG.info("secret : " + secret);
        LOG.info("otp : " + otp);
        String s2sToken = "";
        try {
            s2sToken = s2sApi.serviceToken(microservice, otp);
            LOG.info("s2sToken : " + s2sToken);
            return s2sToken;
        } catch (Exception ex) {
            LOG.error("EXCEPTION in S2sTokenService Notifications-Service !!!");
            LOG.info(ex.getMessage());
        }
        return null;
    }
}
