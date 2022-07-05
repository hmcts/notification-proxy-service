package uk.gov.hmcts.reform.notifications.smoketests;

import io.restassured.RestAssured;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static io.restassured.RestAssured.given;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;

@ComponentScan("uk.gov.hmcts.reform.notifications.smoketests")
@PropertySource("application.properties")
@RunWith(SpringRunner.class)
@Slf4j
public class SmokeTestConfiguration {
    @Value("${TEST_URL:http://localhost:8080}")
    private String testUrl;

    @Before
    public void setup() {
        RestAssured.baseURI = testUrl;
        log.info("Payments-Api base url is :{}", testUrl);
    }

    @Test
    public void shouldReturnChannels() {
        given()
            .relaxedHTTPSValidation()
            .header(CONTENT_TYPE, "application/json")
            .when()
            .get("/refdata/channels")
            .then()
            .statusCode(200);
    }
}
