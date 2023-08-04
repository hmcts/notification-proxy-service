package uk.gov.hmcts.reform.notifications.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import uk.gov.hmcts.reform.notifications.dtos.request.DocPreviewRequest;
import uk.gov.hmcts.reform.notifications.dtos.request.RefundNotificationEmailRequest;
import uk.gov.hmcts.reform.notifications.dtos.request.RefundNotificationLetterRequest;
import uk.gov.hmcts.reform.notifications.dtos.response.NotificationResponseDto;
import uk.gov.hmcts.reform.notifications.dtos.response.NotificationTemplatePreviewResponse;
import uk.gov.hmcts.reform.notifications.dtos.response.PostCodeResponse;
import uk.gov.hmcts.reform.notifications.service.NotificationService;


import javax.validation.Valid;


@RestController
@Tag(name = "Refund Notifications", description = "Refund Notifications REST API")
@Validated
@SuppressWarnings({"PMD.AvoidUncheckedExceptionsInSignatures", "PMD.AvoidDuplicateLiterals"})
public class NotificationController {

    @Autowired
    private NotificationService notificationService;
    private static Logger log = LoggerFactory.getLogger(NotificationController.class);

    @Operation(summary = "Create a email notification for a refund", description = "Create email notification for a refund")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Notification sent successfully via email"),
        @ApiResponse(responseCode = "400", description = "Bad request. Notification creation failed"),
        @ApiResponse(responseCode = "403", description = "AuthError"),
        @ApiResponse(responseCode = "422", description = "Invalid Template ID"),
        @ApiResponse(responseCode = "429", description = "Too Many Requests Error"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error"),
        @ApiResponse(responseCode = "504", description = "Unable to connect to notification provider, please try again late")
    })
    @PostMapping("/notifications/email")
    public ResponseEntity emailNotification(
         @RequestHeader("Authorization") String authorization,
         @RequestHeader(required = false) MultiValueMap<String, String> headers,
         @Valid @RequestBody RefundNotificationEmailRequest request) {
        log.info("recipientEmailAddress in request  for /email endpoint {}",request.getRecipientEmailAddress());
        log.info("reference in request  for /email endpoint {}",request.getReference());
        notificationService.sendEmailNotification(request, headers);
            return new ResponseEntity<>(
                    "Notification sent successfully via email", HttpStatus.CREATED);
    }

    @Operation(summary = "Create a letter notification for a refund", description = "Create letter notification for a refund")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Notification sent successfully via letter"),
        @ApiResponse(responseCode = "400", description = "Bad request. Notification creation failed"),
        @ApiResponse(responseCode = "403", description = "AuthError"),
        @ApiResponse(responseCode = "422", description = "Invalid Template ID"),
        @ApiResponse(responseCode = "422", description = "Please enter a valid/real postcode"),
        @ApiResponse(responseCode = "429", description = "Too Many Requests Error"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error"),
        @ApiResponse(responseCode = "504", description = "Unable to connect to notification provider, please try again late")
    })
    @PostMapping("/notifications/letter")
    public ResponseEntity letterNotification(
        @RequestHeader("Authorization") String authorization,
        @RequestHeader(required = false) MultiValueMap<String, String> headers,
        @Valid @RequestBody RefundNotificationLetterRequest request) {
        log.info("Inside /notifications/letter {}",request);
        log.info("recipientPostalAddress in request  for /letter endpoint {}",request.getRecipientPostalAddress());
        log.info("reference in request  for /letter endpoint {}",request.getReference());
            notificationService.sendLetterNotification(request, headers );
        return new ResponseEntity<>(
            "Notification sent successfully via letter", HttpStatus.CREATED);
    }

    @Operation(summary = "GET /notifications ", description = "Get Notification by passing reference")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Success"),
        @ApiResponse(responseCode = "404", description = "Notification has not been sent for this refund"),
        @ApiResponse(responseCode = "403", description = "AuthError"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")

    })

    @GetMapping("/notifications/{reference}")
    public ResponseEntity<NotificationResponseDto> getNotifications(
        @RequestHeader("Authorization") String authorization,
        @RequestHeader(required = false) MultiValueMap<String, String> headers,
        @PathVariable("reference") String reference) {
        log.info("Notification reference in GET endpoint /notifications/{reference} {}", reference);
        return new ResponseEntity<>(
            notificationService.getNotification(reference),
            HttpStatus.OK
        );
    }

    @Operation(summary = "POST /doc-preview ", description = "Preview Notification by passing personalisation")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Success"),
        @ApiResponse(responseCode = "403", description = "AuthError"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @PostMapping("/notifications/doc-preview")
    public ResponseEntity<NotificationTemplatePreviewResponse> previewNotification(
        @RequestHeader("Authorization") String authorization,
        @RequestHeader(required = false) MultiValueMap<String, String> headers,
        @Valid @RequestBody DocPreviewRequest docPreviewRequest) {
        log.info("Doc Preview Hit");
        return new ResponseEntity<>(
            notificationService.previewNotification(docPreviewRequest,headers),
            HttpStatus.OK
        );
    }

    @Operation(summary = "GET /notifications ", description = "Get Address by passing post code")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Success"),
        @ApiResponse(responseCode = "404", description = "Postcode not found"),
        @ApiResponse(responseCode = "403", description = "AuthError"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")

    })

    @GetMapping("/notifications/postcode-lookup/{postcode}")
    public ResponseEntity<PostCodeResponse> gePostLookUp(
        @RequestHeader("Authorization") String authorization,
        @RequestHeader(required = false) MultiValueMap<String, String> headers,
        @PathVariable("postcode") String postCode) {
        log.info("Notification reference in GET endpoint /notifications/postcode-lookup {}", postCode);
        return new ResponseEntity<PostCodeResponse>(
             notificationService.getAddress(postCode),
            HttpStatus.OK
        );
    }


    @DeleteMapping("/notifications/{reference}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteNotification(@RequestHeader("Authorization") String authorization, @PathVariable String reference) {
        notificationService.deleteNotification(reference);
        log.info("Deleted records >>  {}",reference);
    }

}

