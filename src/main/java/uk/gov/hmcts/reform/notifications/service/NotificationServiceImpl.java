package uk.gov.hmcts.reform.notifications.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.notifications.controllers.ExceptionHandlers;
import uk.gov.hmcts.reform.notifications.dtos.request.Personalisation;
import uk.gov.hmcts.reform.notifications.dtos.request.RefundNotificationEmailRequest;
import uk.gov.hmcts.reform.notifications.dtos.request.RefundNotificationLetterRequest;
import uk.gov.hmcts.reform.notifications.mapper.EmailNotificationMapper;
import uk.gov.hmcts.reform.notifications.mapper.LetterNotificationMapper;
import uk.gov.hmcts.reform.notifications.model.Notification;
import uk.gov.hmcts.reform.notifications.repository.NotificationRepository;
import uk.gov.hmcts.reform.notifications.util.GovNotifyExceptionWrapper;
import uk.gov.service.notify.*;

import java.util.Map;
@Service
public class NotificationServiceImpl implements NotificationService {


    @Autowired
    NotificationRepository notificationRepository;

    @Autowired
    EmailNotificationMapper emailNotificationMapper;

    @Autowired
    LetterNotificationMapper letterNotificationMapper;

    @Autowired
    @Qualifier("Email")
    private NotificationClientApi notificationEmailClient;

    @Autowired
    @Qualifier("Letter")
    private NotificationClientApi notificationLetterClient;

    private static final Logger LOG = LoggerFactory.getLogger(ExceptionHandlers.class);

    @Override
    public SendEmailResponse sendEmailNotification(RefundNotificationEmailRequest emailNotificationRequest) {
        try {
            SendEmailResponse sendEmailResponse = notificationEmailClient
                .sendEmail(
                    emailNotificationRequest.getTemplateId(),
                    emailNotificationRequest.getRecipientEmailAddress(),
                    createEmailPersonalisation(emailNotificationRequest.getPersonalisation()),
                    emailNotificationRequest.getReference()
                );

            Notification notification = emailNotificationMapper.emailResponseMapper(
                emailNotificationRequest
            );
            notificationRepository.save(notification);

            return sendEmailResponse;
        }catch (NotificationClientException exception){
            GovNotifyExceptionWrapper exceptionWrapper = new GovNotifyExceptionWrapper();
            LOG.error(exception.getMessage());
            throw exceptionWrapper.mapGovNotifyEmailException(exception);
        }
    }

    @Override
    public SendLetterResponse sendLetterNotification(RefundNotificationLetterRequest letterNotificationRequest) {

        try {
            SendLetterResponse sendLetterResponse = notificationLetterClient.sendLetter(
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
        }catch (NotificationClientException exception){
            GovNotifyExceptionWrapper exceptionWrapper = new GovNotifyExceptionWrapper();
            LOG.error(exception.getMessage());
            throw exceptionWrapper.mapGovNotifyLetterException(exception);
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
