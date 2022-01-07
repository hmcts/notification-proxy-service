package uk.gov.hmcts.reform.notifications.util;

import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.notifications.exceptions.*;
import uk.gov.service.notify.NotificationClientException;

public class GovNotifyExceptionWrapper {


    GovNotifyErrorMessage govNotifyErrorMessage = new GovNotifyErrorMessage();

    public Exception mapGovNotifyEmailException(NotificationClientException notificationException){
        switch (notificationException.getHttpResult()) {
            case 400:
                String errorMessage = govNotifyErrorMessage.getErrorMessage(notificationException.getMessage());

                if (errorMessage.equals("template_id is not a valid UUID")) {
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

    public Exception mapGovNotifyLetterException(NotificationClientException notificationException){
        switch (notificationException.getHttpResult()) {
            case 400:
                String errorMessage = govNotifyErrorMessage.getErrorMessage(notificationException.getMessage());

                if (errorMessage.equals("Must be a real UK postcode") ||
                    errorMessage.equals("Last line of address must be a real UK postcode or another country")){
                    return new InvalidAddressException("Please enter a valid/real postcode");
                }else if (errorMessage.equals("template_id is not a valid UUID")) {
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
}
