package uk.gov.hmcts.reform.notification.functional.config;


import feign.Feign;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.idam.client.models.TokenExchangeResponse;
import uk.gov.hmcts.reform.idam.client.models.test.CreateUserRequest;
import uk.gov.hmcts.reform.idam.client.models.test.UserGroup;
import uk.gov.hmcts.reform.idam.client.models.test.UserRole;

import java.util.UUID;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@Service
public class IdamService {
    public static final String CMC_CITIZEN_GROUP = "cmc-private-beta";
    public static final String CMC_CASE_WORKER_GROUP = "caseworker";
    public static final String REFUNDS_USER = "caseworker";

    public static final String BEARER = "Bearer ";
    public static final String AUTHORIZATION_CODE = "authorization_code";
    public static final String CODE = "code";
    public static final String BASIC = "Basic ";
    public static final String GRANT_TYPE = "password";
    public static final String SCOPES = "openid profile roles";
    public static final String SCOPES_SEARCH_USER = "openid profile roles search-user";
    private static final Logger LOG = LoggerFactory.getLogger(IdamService.class);
    private final IdamApi idamApi;
    private final TestConfigProperties testConfig;

    @Autowired
    public IdamService(TestConfigProperties testConfig) {
        this.testConfig = testConfig;
        idamApi = Feign.builder()
            .encoder(new JacksonEncoder())
            .decoder(new JacksonDecoder())
            .target(IdamApi.class, testConfig.getIdamApiUrl());
    }

    public ValidUser createUserWith(String userGroup, String... roles) {
        String email = nextUserEmail();
        CreateUserRequest userRequest = userRequest(email, userGroup, roles);
        try {
            idamApi.createUser(userRequest);
        } catch (Exception ex) {
            LOG.info(ex.getMessage());
        }

        String accessToken = authenticateUser(email, testConfig.getTestUserPassword());

        return new ValidUser(email, accessToken);
    }

    public ValidUser createUserWithSearchScope(String userGroup, String... roles) {
        String email = nextUserEmail();
        CreateUserRequest userRequest = userRequest(email, userGroup, roles);
        try {
            idamApi.createUser(userRequest);
        } catch (Exception ex) {
            LOG.error("ERROR in createUserWithSearchScope !!!");
            LOG.info(ex.getMessage());
        }

        String accessToken = authenticateUserWithSearchScope(email, testConfig.getTestUserPassword());
        return new ValidUser(email, accessToken);
    }

    public String authenticateUser(String username, String password) {
        String authorisation = username + ":" + password;
        try {
            TokenExchangeResponse tokenExchangeResponse = idamApi.exchangeCode(username,
                                                                               password,
                                                                               SCOPES,
                                                                               GRANT_TYPE,
                                                                               testConfig.getOauth2().getClientId(),
                                                                               testConfig.getOauth2().getClientSecret(),
                                                                               testConfig.getOauth2().getRedirectUrl());

            return BEARER + tokenExchangeResponse.getAccessToken();
        } catch (Exception ex) {
            LOG.info(ex.getMessage());
        }
        return null;
    }

    public String authenticateUserWithSearchScope(String username, String password) {
        String authorisation = username + ":" + password;
        try {
            TokenExchangeResponse tokenExchangeResponse = idamApi.exchangeCode(username,
                                                                               password,
                                                                               SCOPES_SEARCH_USER,
                                                                               GRANT_TYPE,
                                                                               testConfig.getIdamPayBubbleClientID(),
                                                                               testConfig.getIdamPayBubbleClientSecret(),
                                                                               testConfig.getOauth2().getRedirectUrl());

            return BEARER + tokenExchangeResponse.getAccessToken();
        } catch (Exception ex) {
            LOG.error("ERROR in authenticateUserWithSearchScope !!!");
            LOG.info(ex.getMessage());
        }
        return null;
    }

    private CreateUserRequest userRequest(String email, String userGroup, String... roles) {
        return CreateUserRequest.builder()
            .email(email)
            .password(testConfig.getTestUserPassword())
            .roles(Stream.of(roles)
                       .map(UserRole::new)
                       .collect(toList()))
            .userGroup(new UserGroup(userGroup))
            .build();
    }


    public ValidUser createUserAuthToken(String email) {
        String accessToken = authenticateUser(email, testConfig.getTestUserPassword());
        return new ValidUser(email, accessToken);
    }

    private String nextUserEmail() {
        return String.format(testConfig.getGeneratedUserEmailPattern(), UUID.randomUUID().toString());
    }
}
