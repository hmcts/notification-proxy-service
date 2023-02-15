package uk.gov.hmcts.reform.notifications.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
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
@Api(tags = {"Notification Journey "})
@SuppressWarnings({"PMD.AvoidUncheckedExceptionsInSignatures", "PMD.AvoidDuplicateLiterals"})
public class NotificationController {

    @Autowired
    private NotificationService notificationService;
    private static Logger log = LoggerFactory.getLogger(NotificationController.class);


    @ApiOperation(value = "Create a email notification for a refund", notes = "Create email notification for a refund")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Notification sent successfully via email"),
        @ApiResponse(code = 400, message = "Bad request. Notification creation failed"),
        @ApiResponse(code = 403, message = "AuthError"),
        @ApiResponse(code = 422, message = "Invalid Template ID"),
        @ApiResponse(code = 429, message = "Too Many Requests Error"),
        @ApiResponse(code = 500, message = "Internal Server Error"),
        @ApiResponse(code = 504, message = "Unable to connect to notification provider, please try again late")
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

    @ApiOperation(value = "Create a letter notification for a refund", notes = "Create letter notification for a refund")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Notification sent successfully via letter"),
        @ApiResponse(code = 400, message = "Bad request. Notification creation failed"),
        @ApiResponse(code = 403, message = "AuthError"),
        @ApiResponse(code = 422, message = "Invalid Template ID"),
        @ApiResponse(code = 422, message = "Please enter a valid/real postcode"),
        @ApiResponse(code = 429, message = "Too Many Requests Error"),
        @ApiResponse(code = 500, message = "Internal Server Error"),
        @ApiResponse(code = 504, message = "Unable to connect to notification provider, please try again late")
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

    @ApiOperation(value = "GET /notifications ", notes = "Get Notification by passing reference")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Success"),
        @ApiResponse(code = 404, message = "Notification has not been sent for this refund"),
        @ApiResponse(code = 403, message = "AuthError"),
        @ApiResponse(code = 500, message = "Internal Server Error")

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

    @ApiOperation(value = "POST /doc-preview ", notes = "Preview Notification by passing personalisation")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Success"),
        @ApiResponse(code = 403, message = "AuthError"),
        @ApiResponse(code = 500, message = "Internal Server Error")
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

    @ApiOperation(value = "GET /notifications ", notes = "Get Address by passing post code")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Success"),
        @ApiResponse(code = 404, message = "Postcode not found"),
        @ApiResponse(code = 403, message = "AuthError"),
        @ApiResponse(code = 500, message = "Internal Server Error")

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

