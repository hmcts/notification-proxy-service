package uk.gov.hmcts.reform.notifications.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import uk.gov.hmcts.reform.notifications.config.security.authcheckerconfiguration.AuthCheckerConfiguration;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.MOCK;
import javax.servlet.http.HttpServletRequest;
import java.util.Optional;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles({"local", "test"})
@SpringBootTest(webEnvironment = MOCK)
@ContextConfiguration(loader= AnnotationConfigContextLoader.class,classes = AuthCheckerConfiguration.class)
@SuppressWarnings("PMD")
public class AuthCheckerConfigurationTest {

    @Autowired
    Function<HttpServletRequest, Optional<String>> userIdExtractor;

    @Test
    public void testUserIdExtractor(){
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServerName("www.example.com");
        request.setRequestURI("/users/test/test1");
        request.setQueryString("param1=value1&param");
        Optional<String> value = userIdExtractor.apply(request);
        assertThat(value.get()).isEqualTo("test");
    }
}

