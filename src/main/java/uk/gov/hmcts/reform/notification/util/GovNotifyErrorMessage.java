package uk.gov.hmcts.reform.notification.util;

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

    //Extracts the status from the GovNotify Response body
    public String getStatusCode(String responseBody){
        String message = responseBody.replace('"', ' ');
        String[] splits=message.split("Status code: ");
        message = splits[1];
        splits=message.split(" ");

        message = splits[0];


        return message;
    }

    public int validateStatusCode(String responseBody, int httpResult) {
        int result = httpResult;

        int statusCode = Integer.parseInt(getStatusCode(responseBody));
        if (statusCode != httpResult) {
            result = statusCode;
        }
        return result;
    }

}
