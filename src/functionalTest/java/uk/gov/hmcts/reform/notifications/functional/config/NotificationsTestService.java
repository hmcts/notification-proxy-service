package uk.gov.hmcts.reform.notifications.functional.config;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import uk.gov.hmcts.reform.notifications.dtos.request.RefundNotificationEmailRequest;
import uk.gov.hmcts.reform.notifications.dtos.request.RefundNotificationLetterRequest;

import java.util.HashMap;
import java.util.Map;
import javax.inject.Named;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

public class NotificationsTestService {

    private final Map<String, String> authHeaders = new HashMap<>();

    public Response postEmailNotification(final String userToken,
                                   final String serviceToken,
                                   final String baseUri,
                                   final RefundNotificationEmailRequest request) {
        return givenWithAuthHeaders(userToken, serviceToken)
            .contentType(ContentType.JSON)
            .baseUri(baseUri)
            .body(request)
            .when()
            .post("/emailNotification");
    }

    public Response postLetterNotification(final String userToken,
                                          final String serviceToken,
                                          final String baseUri,
                                          final RefundNotificationLetterRequest request) {
        return givenWithAuthHeaders(userToken, serviceToken)
            .contentType(ContentType.JSON)
            .baseUri(baseUri)
            .body(request)
            .when()
            .post("/letterNotification");
    }

    public RequestSpecification givenWithAuthHeaders(final String userToken, final String serviceToken) {
        return RestAssured.given()
            .header(AUTHORIZATION, userToken)
            .header("ServiceAuthorization", serviceToken);
    }
}
