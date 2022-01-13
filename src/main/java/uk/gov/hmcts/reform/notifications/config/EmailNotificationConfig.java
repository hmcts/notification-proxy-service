package uk.gov.hmcts.reform.notifications.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientApi;

@Configuration
public class EmailNotificationConfig {
    @Value("${notify.email.apiKey}")
    private String notificationApiKeyEmail;

    @Value("${notify.letter.apiKey}")
    private String notificationApiKeyLetter;

    @Bean("Email")
    public NotificationClientApi notificationEmailClient(){
        return new NotificationClient(notificationApiKeyEmail);

    }

    @Bean("Letter")
    public NotificationClientApi notificationLetterClient(){
        return new NotificationClient(notificationApiKeyLetter);

    }

}
