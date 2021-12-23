package uk.gov.hmcts.reform.notifications.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.notifications.dtos.request.EmailNotificationRequest;
import uk.gov.hmcts.reform.notifications.mapper.EmailNotificationMapper;
import uk.gov.hmcts.reform.notifications.model.Notification;
import uk.gov.hmcts.reform.notifications.repository.NotificationRepository;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientApi;
import uk.gov.service.notify.NotificationClientException;
import uk.gov.service.notify.SendEmailResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class NotificationServiceImpl implements NotificationService {

    @Value("${notify.email.apiKey}")
    private String notificationApiKeyEmail;

    @Value("${notify.letter.apiKey}")
    private String notificationApiKeyLetter;

    @Autowired
    NotificationRepository notificationRepository;

    @Autowired
    EmailNotificationMapper emailNotificationMapper;

    @Override
    public SendEmailResponse sendEmailNotification(EmailNotificationRequest emailNotificationRequest, String templateId)
        throws NotificationClientException {
        NotificationClientApi notificationEmailClient = new NotificationClient(notificationApiKeyEmail);
        SendEmailResponse sendEmailResponse =  notificationEmailClient
            .sendEmail(templateId,
                       emailNotificationRequest.getEmail(),
                       createPersonalisation(emailNotificationRequest.getName(), emailNotificationRequest.getRefundReference()),
                       String.format("hrs-grant-%s",  UUID.randomUUID()));

        Notification notification = emailNotificationMapper.emailResponseMapper(sendEmailResponse);
        notificationRepository.save(notification);
        return sendEmailResponse;
    }

    @Override
    public void sendLetterNotification() throws NotificationClientException {
        String templateId = "7ed517e8-b34d-4aa6-8822-afb578a0a69d";
        NotificationClientApi notificationletterClient = new NotificationClient(notificationApiKeyLetter);

        Map<String, Object> personalisation = new HashMap<>();
        personalisation.put("address_line_1", "The Occupier"); // mandatory address field
        personalisation.put("address_line_2", "Flat 2"); // mandatory address field
        personalisation.put("address_line_3", "India"); // mandatory address field, must be a real UK postcode
        personalisation.put("first_name", "Anshika"); // field from template
        personalisation.put("application_date", "2018-01-01"); // field from template

        notificationletterClient.sendLetter(
            templateId,
            personalisation,
            "Testing"
        );

    }

    private Map<String, Object> createPersonalisation(String name, String refundReferenceNumber) {

        return Map.of("name", name,
                      "refnumber", refundReferenceNumber);
    }
}
