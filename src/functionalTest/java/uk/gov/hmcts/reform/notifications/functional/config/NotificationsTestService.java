package uk.gov.hmcts.reform.notifications.functional.config;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import net.serenitybdd.rest.SerenityRest;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.notifications.dtos.request.DocPreviewRequest;
import uk.gov.hmcts.reform.notifications.dtos.request.RefundNotificationEmailRequest;
import uk.gov.hmcts.reform.notifications.dtos.request.RefundNotificationLetterRequest;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Component
public class NotificationsTestService {

    public Response postEmailNotification(final String userToken,
                                          final String serviceToken,
                                          final String baseUri,
                                          final RefundNotificationEmailRequest request) {
        return givenWithAuthHeaders(userToken, serviceToken)
            .contentType(ContentType.JSON)
            .body(request)
            .baseUri(baseUri)
            .when()
            .post("/notifications/email");
    }

    public Response postLetterNotification(final String userToken,
                                           final String serviceToken,
                                           final String baseUri,
                                           final RefundNotificationLetterRequest request) {
        return givenWithAuthHeaders(userToken, serviceToken)
            .contentType(ContentType.JSON)
            .body(request)
            .baseUri(baseUri)
            .when()
            .post("/notifications/letter");
    }

    public Response getNotification(final String userToken,
                                    final String serviceToken,
                                    final String baseUri,
                                    final String reference) {
        return givenWithAuthHeaders(userToken, serviceToken)
            .baseUri(baseUri)
            .contentType(ContentType.JSON)
            .when()
            .get("/notifications/{reference}", reference);
    }

    public Response deleteNotification(final String userToken, final String serviceToken,
                                       final String baseUri,
                                 final String reference) {
        return givenWithAuthHeaders(userToken, serviceToken).baseUri(baseUri).when()
            .delete("/notifications/{reference}", reference);
    }

    public RequestSpecification givenWithAuthHeaders(final String userToken, final String serviceToken) {
        return SerenityRest.given()
            .header(AUTHORIZATION, userToken)
            .header("ServiceAuthorization", serviceToken);
    }


    public Response getTemplateNotificationPreview(final String userToken,
                                           final String serviceToken,
                                           final String baseUri,
                                           final DocPreviewRequest request) {
        return givenWithAuthHeaders(userToken, serviceToken)
            .contentType(ContentType.JSON)
            .body(request)
            .baseUri(baseUri)
            .when()
            .post("/notifications/doc-preview");
    }

    public Response getPostCodeLookup(final String userToken,
                                    final String serviceToken,
                                    final String baseUri,
                                    final String postCode) {
        return givenWithAuthHeaders(userToken, serviceToken)
            .baseUri(baseUri)
            .contentType(ContentType.JSON)
            .when()
            .get("/notifications/postcode-lookup/{postcode}", postCode);
    }
}
