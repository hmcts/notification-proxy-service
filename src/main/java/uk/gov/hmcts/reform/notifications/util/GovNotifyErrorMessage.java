package uk.gov.hmcts.reform.notifications.util;

import org.springframework.stereotype.Component;

public class GovNotifyErrorMessage {

    //Extracts the error message from the GovNotify Response body
    public String getErrorMessage(String responseBody){
        String message = responseBody.replace('"', ' ');
        String[] splits=message.split("message : ");
        message = splits[1];
        splits=message.split(" }");

        message = splits[0];
        return message;
    }
}
