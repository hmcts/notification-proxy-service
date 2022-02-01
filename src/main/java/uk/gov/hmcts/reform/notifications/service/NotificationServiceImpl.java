package uk.gov.hmcts.reform.notifications.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import uk.gov.hmcts.reform.notifications.config.security.idam.IdamService;
import uk.gov.hmcts.reform.notifications.controllers.ExceptionHandlers;
import uk.gov.hmcts.reform.notifications.dtos.request.Personalisation;
import uk.gov.hmcts.reform.notifications.dtos.request.RefundNotificationEmailRequest;
import uk.gov.hmcts.reform.notifications.dtos.request.RefundNotificationLetterRequest;
import uk.gov.hmcts.reform.notifications.dtos.response.IdamUserIdResponse;
import uk.gov.hmcts.reform.notifications.dtos.response.NotificationResponseDto;
import uk.gov.hmcts.reform.notifications.exceptions.NotificationListEmptyException;
import uk.gov.hmcts.reform.notifications.mapper.EmailNotificationMapper;
import uk.gov.hmcts.reform.notifications.mapper.LetterNotificationMapper;
import uk.gov.hmcts.reform.notifications.mapper.NotificationResponseMapper;
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

    @Autowired
    NotificationResponseMapper notificationResponseMapper;

    @Autowired
    private IdamService idamService;

    private NotificationResponseDto notificationResponseDto;

    private static final Logger LOG = LoggerFactory.getLogger(ExceptionHandlers.class);

    @Override
    public SendEmailResponse sendEmailNotification(RefundNotificationEmailRequest emailNotificationRequest, MultiValueMap<String, String> headers) {
        try {
            IdamUserIdResponse uid = idamService.getUserId(headers);
            SendEmailResponse sendEmailResponse = notificationEmailClient
                .sendEmail(
                    emailNotificationRequest.getTemplateId(),
                    emailNotificationRequest.getRecipientEmailAddress(),
                    createEmailPersonalisation(emailNotificationRequest.getPersonalisation()),
                    emailNotificationRequest.getReference()
                );

            Notification notification = emailNotificationMapper.emailResponseMapper(
                emailNotificationRequest, uid
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
    public SendLetterResponse sendLetterNotification(RefundNotificationLetterRequest letterNotificationRequest, MultiValueMap<String, String> headers) {

        try {
            IdamUserIdResponse uid = idamService.getUserId(headers);
            SendLetterResponse sendLetterResponse = notificationLetterClient.sendLetter(
                letterNotificationRequest.getTemplateId(),
                createLetterPersonalisation(letterNotificationRequest),
                letterNotificationRequest.getReference()
            );

            Notification notification = letterNotificationMapper.letterResponseMapper(
                sendLetterResponse,
                letterNotificationRequest,
                uid
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

    @Override
    public NotificationResponseDto getNotification(String reference) {

        Optional<List<Notification>> notificationList;
        notificationList = notificationRepository.findByReferenceOrderByDateUpdatedDesc(reference);

        LOG.info("notificationList: {}", notificationList);

        if (notificationList.isPresent() && !notificationList.get().isEmpty()) {

            notificationResponseDto = NotificationResponseDto
                .buildNotificationListWith()
                .notifications(notificationList.get().stream().map(notificationResponseMapper::notificationResponse)
                                   .collect(Collectors.toList()))
                .build();
        }else {
            throw new NotificationListEmptyException("Notification has not been sent for this refund");
        }
        return notificationResponseDto;
    }
}
