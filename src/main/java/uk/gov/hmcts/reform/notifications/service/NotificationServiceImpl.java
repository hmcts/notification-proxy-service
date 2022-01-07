package uk.gov.hmcts.reform.notifications.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.notifications.controllers.ExceptionHandlers;
import uk.gov.hmcts.reform.notifications.dtos.request.EmailNotificationRequest;
import uk.gov.hmcts.reform.notifications.dtos.request.Personalisation;
import uk.gov.hmcts.reform.notifications.dtos.request.RefundNotificationEmailRequest;
import uk.gov.hmcts.reform.notifications.dtos.request.RefundNotificationLetterRequest;
import uk.gov.hmcts.reform.notifications.mapper.EmailNotificationMapper;
import uk.gov.hmcts.reform.notifications.mapper.LetterNotificationMapper;
import uk.gov.hmcts.reform.notifications.model.Notification;
import uk.gov.hmcts.reform.notifications.repository.NotificationRepository;
import uk.gov.hmcts.reform.notifications.util.GovNotifyExceptionWrapper;
import uk.gov.service.notify.*;

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

    @Autowired
    LetterNotificationMapper letterNotificationMapper;

    private static final Logger LOG = LoggerFactory.getLogger(ExceptionHandlers.class);

    @Override
    public SendEmailResponse sendEmailNotification(RefundNotificationEmailRequest emailNotificationRequest)

        throws Exception {
        NotificationClientApi notificationEmailClient = new NotificationClient(notificationApiKeyEmail);
        try {
            SendEmailResponse sendEmailResponse = notificationEmailClient
                .sendEmail(
                    emailNotificationRequest.getTemplateId(),
                    emailNotificationRequest.getRecipientEmailAddress(),
                    createEmailPersonalisation(emailNotificationRequest.getPersonalisation()),
                    emailNotificationRequest.getReference()
                );

//                       String.format("hrs-grant-%s",  UUID.randomUUID()));

            Notification notification = emailNotificationMapper.emailResponseMapper(
                sendEmailResponse,
                emailNotificationRequest
            );
            notificationRepository.save(notification);

            return sendEmailResponse;
        }catch (NotificationClientException e){
            GovNotifyExceptionWrapper exceptionWrapper = new GovNotifyExceptionWrapper();
            LOG.error(e.getMessage());
            throw exceptionWrapper.mapGovNotifyEmailException(e);
        }
    }

    @Override
    public SendLetterResponse sendLetterNotification(RefundNotificationLetterRequest letterNotificationRequest) throws Exception {
        NotificationClientApi notificationletterClient = new NotificationClient(notificationApiKeyLetter);

//        Map<String, Object> personalisation = new HashMap<>();
//        personalisation.put("address_line_1", "The Occupier"); // mandatory address field
//        personalisation.put("address_line_2", "Flat 2"); // mandatory address field
//        personalisation.put("address_line_3", "India"); // mandatory address field, must be a real UK postcode
//        personalisation.put("first_name", "Anshika"); // field from template
//        personalisation.put("application_date", "2018-01-01"); // field from template
        try {
            SendLetterResponse sendLetterResponse = notificationletterClient.sendLetter(
                letterNotificationRequest.getTemplateId(),
                createLetterPersonalisation(letterNotificationRequest),
                letterNotificationRequest.getReference()
            );

            Notification notification = letterNotificationMapper.letterResponseMapper(
                sendLetterResponse,
                letterNotificationRequest
            );
            notificationRepository.save(notification);
            return sendLetterResponse;
        }catch (NotificationClientException e){
            GovNotifyExceptionWrapper exceptionWrapper = new GovNotifyExceptionWrapper();
            LOG.error(e.getMessage());
            throw exceptionWrapper.mapGovNotifyLetterException(e);
        }
    }

    private Map<String, Object> createEmailPersonalisation(Personalisation personalisation) {

        return Map.of("name", "Unknown",
                      "refnumber", personalisation.getRefundReference(),
                      "ccd_case_number", personalisation.getCcdCaseNumber(),
                      "service_url", personalisation.getServiceUrl(),
                      "service_mailbox", personalisation.getServiceMailBox(),
                      "refund_lag_time", personalisation.getRefundLagTime());
    }

    private Map<String, Object> createLetterPersonalisation(RefundNotificationLetterRequest letterNotificationRequest) {

        return Map.of("address_line_1", letterNotificationRequest.getRecipientPostalAddress().getAddressLine(),
                      "address_line_2", letterNotificationRequest.getRecipientPostalAddress().getCity(),
                      "address_line_3", letterNotificationRequest.getRecipientPostalAddress().getPostalCode(),
                      "first_name", "Unknown",
                      "application_date", "2022-01-01");
    }
}
