package uk.gov.hmcts.reform.notifications.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import uk.gov.hmcts.reform.notifications.dtos.request.EmailNotificationRequest;
import uk.gov.hmcts.reform.notifications.dtos.request.RefundNotificationEmailRequest;
import uk.gov.hmcts.reform.notifications.dtos.request.RefundNotificationLetterRequest;
import uk.gov.hmcts.reform.notifications.service.NotificationService;
import uk.gov.service.notify.NotificationClientException;
import uk.gov.service.notify.SendEmailResponse;
import uk.gov.service.notify.SendLetterResponse;

import javax.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@RestController
@Api(tags = {"Notification Journey "})
@SuppressWarnings({"PMD.AvoidUncheckedExceptionsInSignatures", "PMD.AvoidDuplicateLiterals"})
public class NotificationController {
    @Autowired
    private NotificationService notificationService;

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
    @PostMapping("/emailNotification")
    public ResponseEntity emailNotification(
//         @RequestHeader("Authorization") String authorization,
         @RequestHeader(required = false) MultiValueMap<String, String> headers,
         @Valid @RequestBody RefundNotificationEmailRequest request) throws Exception {
        SendEmailResponse sendEmailResponse = notificationService.sendEmailNotification(request);
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
    @PostMapping("/letterNotification")
    public ResponseEntity letterNotification(
      //  @RequestHeader("Authorization") String authorization,
        @RequestHeader(required = false) MultiValueMap<String, String> headers,
        @Valid @RequestBody RefundNotificationLetterRequest request) throws Exception {
        SendLetterResponse sendLetterResponse = notificationService.sendLetterNotification(request);
        return new ResponseEntity<>(
            "Notification sent successfully via letter", HttpStatus.CREATED);
    }

}

