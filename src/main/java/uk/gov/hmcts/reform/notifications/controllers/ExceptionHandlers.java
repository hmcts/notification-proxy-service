package uk.gov.hmcts.reform.notifications.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import uk.gov.hmcts.reform.notifications.exceptions.*;

import java.util.LinkedList;
import java.util.List;

@ControllerAdvice
public class ExceptionHandlers extends ResponseEntityExceptionHandler {
    private static final Logger LOG = LoggerFactory.getLogger(ExceptionHandlers.class);


    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers, HttpStatus status, WebRequest request) {
        List<String> details = new LinkedList<>();
        for (ObjectError error : ex.getBindingResult().getAllErrors()) {
            details.add(error.getDefaultMessage());
        }
        LOG.error("NotificationValidation error", ex);
        return new ResponseEntity<>(details.get(0), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({ExceededRequestLimitException.class, InvalidApiKeyException.class, RestrictedApiKeyException.class,
        GovNotifyUnmappedException.class, UserNotFoundException.class})
    public ResponseEntity return500(Exception ex) {
        LOG.error(ex.getMessage());
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler({InvalidAddressException.class, InvalidTemplateId.class})
    public ResponseEntity return422(Exception ex) {
        LOG.error(ex.getMessage());
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler({GovNotifyConnectionException.class})
    public ResponseEntity return503(Exception ex) {
        LOG.error(ex.getMessage());
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler({NotificationListEmptyException.class, PostCodeLookUpNotFoundException.class})
    public ResponseEntity return404(Exception ex) {
        LOG.error(ex.getMessage());
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(GatewayTimeoutException.class)
    public ResponseEntity return504(GatewayTimeoutException ex) {
        LOG.error(ex.getMessage());
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.GATEWAY_TIMEOUT);
    }
}
