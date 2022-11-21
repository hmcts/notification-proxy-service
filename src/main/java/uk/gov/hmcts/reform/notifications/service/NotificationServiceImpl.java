package uk.gov.hmcts.reform.notifications.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.reform.notifications.config.security.idam.IdamService;
import uk.gov.hmcts.reform.notifications.controllers.ExceptionHandlers;
import uk.gov.hmcts.reform.notifications.dtos.request.*;
import uk.gov.hmcts.reform.notifications.dtos.response.IdamUserIdResponse;
import uk.gov.hmcts.reform.notifications.dtos.response.NotificationResponseDto;
import uk.gov.hmcts.reform.notifications.dtos.response.NotificationTemplatePreviewResponse;
import uk.gov.hmcts.reform.notifications.exceptions.NotificationListEmptyException;
import uk.gov.hmcts.reform.notifications.mapper.EmailNotificationMapper;
import uk.gov.hmcts.reform.notifications.mapper.LetterNotificationMapper;
import uk.gov.hmcts.reform.notifications.mapper.NotificationResponseMapper;
import uk.gov.hmcts.reform.notifications.mapper.NotificationTemplateResponseMapper;
import uk.gov.hmcts.reform.notifications.model.Notification;
import uk.gov.hmcts.reform.notifications.model.ServiceContact;
import uk.gov.hmcts.reform.notifications.repository.NotificationRepository;
import uk.gov.hmcts.reform.notifications.repository.ServiceContactRepository;
import uk.gov.hmcts.reform.notifications.util.GovNotifyExceptionWrapper;
import uk.gov.service.notify.*;

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

    @Autowired
    private ServiceContactRepository serviceContactRepository;

    @Autowired
    private NotificationTemplateResponseMapper notificationTemplateResponseMapper;

    private NotificationResponseDto notificationResponseDto;

    private static final Logger LOG = LoggerFactory.getLogger(ExceptionHandlers.class);

    private static final String CASH = "cash";

    private static final String POSTAL_ORDER = "postal order";

    private static final String BULK_SCAN = "bulk scan";

    private static final String REFUND_WHEN_CONTACTED = "RefundWhenContacted";

    private static final String SEND_REFUND = "SendRefund";

    private static final String EMAIL = "EMAIL";

    private static final String LETTER = "LETTER";

    private static final String REFUND_REJECT_REASON ="Unable to apply refund to Card";

    @Value("${notify.template.cheque-po-cash.letter}")
    private String chequePoCashLetterTemplateId;

    @Value("${notify.template.cheque-po-cash.email}")
    private String chequePoCashEmailTemplateId;

    @Value("${notify.template.card-pba.letter}")
    private String cardPbaLetterTemplateId;

    @Value("${notify.template.card-pba.email}")
    private String cardPbaEmailTemplateId;


    @Override
    public SendEmailResponse sendEmailNotification(RefundNotificationEmailRequest emailNotificationRequest, MultiValueMap<String, String> headers) {
        try {

            validateRecipientEmailAddress(emailNotificationRequest);
            Optional<ServiceContact> serviceContact = serviceContactRepository.findByServiceName(emailNotificationRequest.getServiceName());
            IdamUserIdResponse uid = idamService.getUserId(headers);
            SendEmailResponse sendEmailResponse = notificationEmailClient
                .sendEmail(
                    emailNotificationRequest.getTemplateId(),
                    getRecipientEmailAddressForRefundWhenContacted(emailNotificationRequest),
                    createEmailPersonalisation(emailNotificationRequest.getPersonalisation(), serviceContact.get().getServiceMailbox(),
                                               emailNotificationRequest.getPersonalisation().getRefundReference()),
                    emailNotificationRequest.getReference()
                );

            Notification notification = emailNotificationMapper.emailResponseMapper(
                emailNotificationRequest, uid
            );
            notificationRepository.save(notification);
            LOG.info("email notification saved successfully.");

            return sendEmailResponse;
        }catch (NotificationClientException exception){
            GovNotifyExceptionWrapper exceptionWrapper = new GovNotifyExceptionWrapper();
            LOG.error(exception.getMessage());
            throw exceptionWrapper.mapGovNotifyEmailException(exception);
        }
    }

    private String getRecipientEmailAddressForRefundWhenContacted(
        RefundNotificationEmailRequest emailNotificationRequest) {

        String email = emailNotificationRequest.getRecipientEmailAddress();
        if(null == email &&
            REFUND_REJECT_REASON.equals(emailNotificationRequest.getPersonalisation().getRefundReason())) {

            //NotificationResponseDto notificationResponseDto = getNotification(emailNotificationRequest.getReference());
            Optional<List<Notification>> notificationList;
            notificationList = notificationRepository.findByReferenceAndNotificationTypeOrderByDateUpdatedDesc(
                emailNotificationRequest.getReference(), EMAIL);
            LOG.info("notificationList: {}", notificationList);

            if (notificationList.isPresent() && !notificationList.get().isEmpty()) {

                Notification notification = notificationList.get().stream().findAny().get();
                LOG.info(" notificationDto string : " + notification.toString());
                LOG.info(" Email id : " + notification.getContactDetails().getEmail());
                email = notification.getContactDetails().getEmail();
                emailNotificationRequest.setRecipientEmailAddress(email);
            }
        }
        return email;
    }

    private RecipientPostalAddress getRecipientContactAddressForRefundWhenContacted(
        RefundNotificationLetterRequest letterNotificationRequest) {

        RecipientPostalAddress recipientPostalAddress = letterNotificationRequest.getRecipientPostalAddress();

        if(REFUND_REJECT_REASON.equals(letterNotificationRequest.getPersonalisation().getRefundReason())) {

            Optional<List<Notification>> notificationList;
            notificationList = notificationRepository.findByReferenceAndNotificationTypeOrderByDateUpdatedDesc(
                letterNotificationRequest.getReference(), LETTER);
            LOG.info("notificationList: {}", notificationList);

            if (notificationList.isPresent() && !notificationList.get().isEmpty()) {
                Notification notification = notificationList.get().stream().findAny().get();
                LOG.info(" notification string : " + notification.toString());
                recipientPostalAddress = RecipientPostalAddress.recipientPostalAddressWith()
                    .addressLine(notification.getContactDetails().getAddressLine())
                    .city(notification.getContactDetails().getCity())
                    .county(notification.getContactDetails().getCountry())
                    .country(notification.getContactDetails().getCounty())
                    .postalCode(notification.getContactDetails().getPostcode())
                    .build();
                letterNotificationRequest.setRecipientPostalAddress(recipientPostalAddress);
                LOG.info(" Recipient address : " + recipientPostalAddress.toString());
            }
        }
        return recipientPostalAddress;
    }

    @Override
    public SendLetterResponse sendLetterNotification(RefundNotificationLetterRequest letterNotificationRequest, MultiValueMap<String, String> headers) {

        try {
            validateRecipientPostalAddress(letterNotificationRequest);
            Optional<ServiceContact> serviceContact = serviceContactRepository.findByServiceName(letterNotificationRequest.getServiceName());
            IdamUserIdResponse uid = idamService.getUserId(headers);
            SendLetterResponse sendLetterResponse = notificationLetterClient.sendLetter(
                letterNotificationRequest.getTemplateId(),
                createLetterPersonalisation(getRecipientContactAddressForRefundWhenContacted(letterNotificationRequest),
                                            letterNotificationRequest.getPersonalisation(),serviceContact.get().getServiceMailbox(),
                                            letterNotificationRequest.getPersonalisation().getRefundReference()          ),
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

    private Map<String, Object> createEmailPersonalisation(Personalisation personalisation, String serviceMailBox, String refundRef) {

        return Map.of("refundReference", refundRef,
                      "ccdCaseNumber", personalisation.getCcdCaseNumber(),
                      "serviceMailbox", serviceMailBox,
                      "refundAmount", personalisation.getRefundAmount(),
                      "reason", personalisation.getRefundReason());
    }

    private Map<String, Object> createLetterPersonalisation(RecipientPostalAddress recipientPostalAddress, Personalisation personalisation, String serviceMailBox,String refundRef) {

        return Map.of("address_line_1", recipientPostalAddress.getAddressLine(),
                      "address_line_2", recipientPostalAddress.getCity(),
                      "address_line_3",recipientPostalAddress.getCounty(),
                      "address_line_4",recipientPostalAddress.getCountry(),
                      "address_line_5", recipientPostalAddress.getPostalCode(),
                      "refundReference", refundRef,
                      "ccdCaseNumber", personalisation.getCcdCaseNumber(),
                      "serviceMailbox", serviceMailBox,
                      "refundAmount", personalisation.getRefundAmount(),
                      "reason", personalisation.getRefundReason());
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

    @Override
    public NotificationTemplatePreviewResponse previewNotification(DocPreviewRequest docPreviewRequest) {
        TemplatePreview templatePreview;
        NotificationTemplatePreviewResponse notificationTemplatePreviewResponse;
        String instructionType = null;
        String refundRef = "RF-****-****-****-****";
        if (docPreviewRequest.getPaymentMethod() != null) {

            if (BULK_SCAN.equals(docPreviewRequest.getPaymentChannel()) && (CASH.equals(docPreviewRequest.getPaymentMethod())
                || POSTAL_ORDER.equals(docPreviewRequest.getPaymentMethod()))) {
                instructionType = REFUND_WHEN_CONTACTED;
            } else {
                instructionType = SEND_REFUND;
            }
        }

        Optional<ServiceContact> serviceContact = serviceContactRepository.findByServiceName(docPreviewRequest.getServiceName());
        String templeteId = getTemplate(docPreviewRequest, instructionType);

        try {

            if(EMAIL.equalsIgnoreCase(docPreviewRequest.getNotificationType().name())) {

                templatePreview = notificationEmailClient
                    .generateTemplatePreview(templeteId,
                                             createEmailPersonalisation(docPreviewRequest.getPersonalisation(), serviceContact.get().getServiceMailbox(),
                                                                        refundRef));
            } else {

                templatePreview = notificationLetterClient
                    .generateTemplatePreview(templeteId,
                                             createLetterPersonalisation(docPreviewRequest.getRecipientPostalAddress(),docPreviewRequest.getPersonalisation(), serviceContact.get().getServiceMailbox(),
                                                                         refundRef));
            }

         notificationTemplatePreviewResponse = notificationTemplateResponseMapper.notificationPreviewResponse(templatePreview,docPreviewRequest);

        } catch (NotificationClientException exception) {
            GovNotifyExceptionWrapper exceptionWrapper = new GovNotifyExceptionWrapper();
            LOG.error(exception.getMessage());
            throw exceptionWrapper.mapGovNotifyPreviewException(exception);
        }
        return notificationTemplatePreviewResponse;
    }

    private  String getTemplate(DocPreviewRequest docPreviewRequest, String instructionType) {
        String templateId = null;
        if (null != docPreviewRequest.getNotificationType()) {

            if (REFUND_WHEN_CONTACTED.equals(instructionType)) {
                if (EMAIL.equalsIgnoreCase(docPreviewRequest.getNotificationType().name())) {
                    templateId = chequePoCashEmailTemplateId;
                } else {
                    templateId = chequePoCashLetterTemplateId;
                }
            } else {
                if (EMAIL.equalsIgnoreCase(docPreviewRequest.getNotificationType().name())) {
                    templateId = cardPbaEmailTemplateId;
                } else {
                    templateId = cardPbaLetterTemplateId;
                }
            }
        }
        return templateId;
    }

    private void validateRecipientEmailAddress(RefundNotificationEmailRequest emailNotificationRequest) {
        if(!REFUND_REJECT_REASON.equalsIgnoreCase(emailNotificationRequest.getPersonalisation().getRefundReason())
            && ( null == emailNotificationRequest.getRecipientEmailAddress()
            ||  emailNotificationRequest.getRecipientEmailAddress().isEmpty())) {
            LOG.error("Recipient Email Address cannot be null or blank");
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "Recipient Email Address cannot be null or blank");
        }
    }

    private void validateRecipientPostalAddress(RefundNotificationLetterRequest letterlNotificationRequest) {
        if(!REFUND_REJECT_REASON.equalsIgnoreCase(letterlNotificationRequest.getPersonalisation().getRefundReason())
            && ( null == letterlNotificationRequest.getRecipientPostalAddress()
            || !validAddress(letterlNotificationRequest.getRecipientPostalAddress()))) {
            LOG.error("Recipient postal Address cannot be null or blank");
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "Recipient postal Address cannot be null or blank");
        }
    }

    private boolean validAddress(RecipientPostalAddress address){
        return !( StringUtils.isBlank(address.getPostalCode()) || StringUtils.isBlank(address.getAddressLine())
            || StringUtils.isBlank(address.getCity()) || StringUtils.isBlank(address.getCountry())
            || StringUtils.isBlank(address.getCounty()) );
    }

}
