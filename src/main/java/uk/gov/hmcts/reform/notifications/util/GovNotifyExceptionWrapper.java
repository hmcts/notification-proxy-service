package uk.gov.hmcts.reform.notifications.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.reform.notifications.controllers.ExceptionHandlers;
import uk.gov.hmcts.reform.notifications.exceptions.*;
import uk.gov.service.notify.NotificationClientException;

public class GovNotifyExceptionWrapper {

    GovNotifyErrorMessage govNotifyErrorMessage = new GovNotifyErrorMessage();
    private static final Logger LOG = LoggerFactory.getLogger(ExceptionHandlers.class);


    static final String INVALID_TEMPLATE_ID_RESPONSE = "template_id is not a valid UUID";
    static final String INVALID_POSTCODE_RESPONSE_ONE = "Must be a real UK postcode";
    static final String INVALID_POSTCODE_RESPONSE_TWO = "Last line of address must be a real UK postcode or another country";

    static final String NO_RESULT_FOUND = "No result found";
    static final String MISSING_PERSONALISATION = "Missing personalisation: [PERSONALISATION FIELD]";
    static final String SYSTEM_CLOCK_ERROR = "Error: Your system clock must be accurate to within 30 seconds";
    static final String INVALID_TOKEN = "Invalid token: API key not found";

    public GovNotifyException mapGovNotifyEmailException(NotificationClientException exception){
        int httpResult = exception.getHttpResult();

        try {
            httpResult = govNotifyErrorMessage.validateStatusCode(exception.getMessage(), exception.getHttpResult());
        }
        catch (Exception ex){
            LOG.error(ex.getMessage());
        }

        switch (httpResult) {
            case 400:
                String errorMessage = govNotifyErrorMessage.getErrorMessage(exception.getMessage());

                if (INVALID_TEMPLATE_ID_RESPONSE.equals(errorMessage)) {
                    return new InvalidTemplateId("Invalid Template ID");
                }else {
                    return new RestrictedApiKeyException("Internal Server Error, restricted API key");
                }
            case 403:
                return new InvalidApiKeyException("Internal Server Error, invalid API key");
            case 429:
                return new ExceededRequestLimitException("Internal Server Error, send limit exceeded");
            case 500:
                return new GovNotifyConnectionException("Service is not available, please try again");
            default:
                return new GovNotifyUnmappedException("Internal Server Error");
        }
    }

    public GovNotifyException mapGovNotifyLetterException(NotificationClientException exception){
        int httpResult = exception.getHttpResult();

        try {
            httpResult = govNotifyErrorMessage.validateStatusCode(exception.getMessage(), exception.getHttpResult());
        }
        catch (Exception ex){
            LOG.error(ex.getMessage());
        }

        switch (httpResult) {
            case 400:
                String errorMessage = govNotifyErrorMessage.getErrorMessage(exception.getMessage());

                if (INVALID_POSTCODE_RESPONSE_ONE.equals(errorMessage) ||
                    INVALID_POSTCODE_RESPONSE_TWO.equals(errorMessage)){
                    return new InvalidAddressException("Please enter a valid/real postcode");
                }else if (INVALID_TEMPLATE_ID_RESPONSE.equals(errorMessage)) {
                    return new InvalidTemplateId("Invalid Template ID");
                }else {
                    return new RestrictedApiKeyException("Internal Server Error, restricted API key");
                }
            case 403:
                return new InvalidApiKeyException("Internal Server Error, invalid API key");
            case 429:
                return new ExceededRequestLimitException("Internal Server Error, send limit exceeded");
            case 500:
                return new GovNotifyConnectionException("Service is not available, please try again");
            default:
                return new GovNotifyUnmappedException("Internal Server Error");
        }
    }

    public GovNotifyException mapGovNotifyPreviewException(NotificationClientException exception) {
        int httpResult = exception.getHttpResult();

        try {
            httpResult = govNotifyErrorMessage.validateStatusCode(exception.getMessage(), exception.getHttpResult());
        } catch (Exception ex) {
            LOG.error(ex.getMessage());
        }

        String errorMessage = govNotifyErrorMessage.getErrorMessage(exception.getMessage());
        switch (httpResult) {
            case 400:
                if (MISSING_PERSONALISATION.equals(errorMessage)) {
                    return new InvalidAddressException("Please enter a valid/real postcode");
                } else if (INVALID_TEMPLATE_ID_RESPONSE.equals(errorMessage)) {
                    return new InvalidTemplateId("Invalid Template ID");
                }
            case 403:
                if (SYSTEM_CLOCK_ERROR.equals(errorMessage)) {
                    return new InvalidAddressException("Check your system clock");
                } else if (INVALID_TOKEN.equals(errorMessage)) {
                    return new InvalidApiKeyException("Internal Server Error, invalid API key");
                }
            default:
                return new GovNotifyUnmappedException("Internal Server Error");
        }
    }
}
