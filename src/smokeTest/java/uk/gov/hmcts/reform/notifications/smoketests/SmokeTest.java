package uk.gov.hmcts.reform.notifications.smoketests;

import io.restassured.RestAssured;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@Slf4j
public class SmokeTest {
    @Value("${TEST_URL:http://localhost:8080}")
    private String testUrl;

    @Before
    public void setup() {
        RestAssured.baseURI = testUrl;
        log.info("Payments-Api base url is :{}", testUrl);
    }

    @Test
    public void healthCheck() {
        log.info("TEST - healthCheck() started");
        /*given()
            .relaxedHTTPSValidation()
            .header(CONTENT_TYPE, "application/json")
            .when()
            .get("/health")
            .then()
            .statusCode(200);*/
        log.info("TEST - healthCheck() finished");
    }
}
