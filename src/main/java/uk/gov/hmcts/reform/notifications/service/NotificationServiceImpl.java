package uk.gov.hmcts.reform.notifications.service;

import java.util.Arrays;
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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.notifications.config.security.idam.IdamService;
import uk.gov.hmcts.reform.notifications.dtos.request.*;
import uk.gov.hmcts.reform.notifications.dtos.response.IdamUserIdResponse;
import uk.gov.hmcts.reform.notifications.dtos.response.NotificationResponseDto;
import uk.gov.hmcts.reform.notifications.dtos.response.NotificationTemplatePreviewResponse;
import uk.gov.hmcts.reform.notifications.dtos.response.PaymentDto;
import uk.gov.hmcts.reform.notifications.exceptions.DocPreviewBadRequestException;
import uk.gov.hmcts.reform.notifications.exceptions.NotificationListEmptyException;
import uk.gov.hmcts.reform.notifications.exceptions.PaymentReferenceNotFoundException;
import uk.gov.hmcts.reform.notifications.exceptions.PaymentServerException;
import uk.gov.hmcts.reform.notifications.mapper.EmailNotificationMapper;
import uk.gov.hmcts.reform.notifications.mapper.LetterNotificationMapper;
import uk.gov.hmcts.reform.notifications.mapper.NotificationResponseMapper;
import uk.gov.hmcts.reform.notifications.mapper.NotificationTemplateResponseMapper;
import uk.gov.hmcts.reform.notifications.model.Notification;
import uk.gov.hmcts.reform.notifications.model.ServiceContact;
import uk.gov.hmcts.reform.notifications.model.TemplatePreviewDto;
import uk.gov.hmcts.reform.notifications.repository.NotificationRepository;
import uk.gov.hmcts.reform.notifications.repository.ServiceContactRepository;
import uk.gov.hmcts.reform.notifications.util.GovNotifyExceptionWrapper;
import uk.gov.service.notify.*;

@Service
@SuppressWarnings({"PMD.TooManyFields", "PMD.ExcessiveImports", "PMD.TooManyMethods", "PMD.GodClass"})
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

    private static final Logger LOG = LoggerFactory.getLogger(NotificationServiceImpl.class);

    private static final String POSTAL_ORDER = "postal order";

    private static final String BULK_SCAN = "bulk scan";

    private static final String REFUND_WHEN_CONTACTED = "RefundWhenContacted";

    private static final String SEND_REFUND = "SendRefund";

    private static final String EMAIL = "EMAIL";

    private static final String LETTER = "LETTER";

    private static final String CASH = "cash";

    private static final String STRING = "string";

    private static final String REFUND_REJECT_REASON ="Unable to apply refund to Card";

    @Value("${notify.template.cheque-po-cash.letter}")
    private String chequePoCashLetterTemplateId;

    @Value("${notify.template.cheque-po-cash.email}")
    private String chequePoCashEmailTemplateId;

    @Value("${notify.template.card-pba.letter}")
    private String cardPbaLetterTemplateId;

    @Value("${notify.template.card-pba.email}")
    private String cardPbaEmailTemplateId;

    @Value("${payments.api.url}")
    private String paymentApiUrl;

    public static final String CONTENT_TYPE = "content-type";

    @Qualifier("restTemplatePayment")
    @Autowired()
    private RestTemplate restTemplatePayment;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Override
    public SendEmailResponse sendEmailNotification(RefundNotificationEmailRequest emailNotificationRequest, MultiValueMap<String, String> headers) {
        try {
            LOG.info("sendEmailNotification -->" +emailNotificationRequest.toString());
            validateRecipientEmailAddress(emailNotificationRequest);
            Optional<ServiceContact> serviceContact = serviceContactRepository.findByServiceName(emailNotificationRequest.getServiceName());
            IdamUserIdResponse uid = idamService.getUserId(headers);

            TemplatePreviewDto templatePreviewDto = emailNotificationRequest.getTemplatePreview();

            if (templatePreviewDto == null) {
                TemplatePreview templatePreview = notificationEmailClient
                    .generateTemplatePreview(
                        emailNotificationRequest.getTemplateId(),
                        createEmailPersonalisation(emailNotificationRequest.getPersonalisation(), serviceContact.get().getServiceMailbox(),
                                                   emailNotificationRequest.getPersonalisation().getRefundReference(),
                                                   emailNotificationRequest.getPersonalisation().getCcdCaseNumber())
                    );
                templatePreviewDto = buildTemplatePreviewDTO(templatePreview, EMAIL);
            }

            SendEmailResponse sendEmailResponse = notificationEmailClient
                .sendEmail(
                    emailNotificationRequest.getTemplateId(),
                    getRecipientEmailAddressForRefundWhenContacted(emailNotificationRequest),
                    createEmailPersonalisation(emailNotificationRequest.getPersonalisation(), serviceContact.get().getServiceMailbox(),
                                               emailNotificationRequest.getPersonalisation().getRefundReference(),
                                               emailNotificationRequest.getPersonalisation().getCcdCaseNumber()),
                    emailNotificationRequest.getReference()
                );

            Notification notification = emailNotificationMapper.emailResponseMapper(
                emailNotificationRequest, uid
            );

            notification.setTemplatePreview(templatePreviewDto);
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
            REFUND_REJECT_REASON.equalsIgnoreCase(emailNotificationRequest.getPersonalisation().getRefundReason())) {
            Optional<List<Notification>> notificationList;
            notificationList = notificationRepository.findByReferenceAndNotificationTypeOrderByDateUpdatedDesc(
                emailNotificationRequest.getReference(), EMAIL);

            if (notificationList.isPresent() && !notificationList.get().isEmpty()) {

                Notification notification = notificationList.get().stream().findAny().get();
                email = notification.getContactDetails().getEmail();
                emailNotificationRequest.setRecipientEmailAddress(email);
            }
        }
        return email;
    }

    private RecipientPostalAddress getRecipientContactAddressForRefundWhenContacted(
        RefundNotificationLetterRequest letterNotificationRequest) {

        RecipientPostalAddress recipientPostalAddress = letterNotificationRequest.getRecipientPostalAddress();

        if(REFUND_REJECT_REASON.equalsIgnoreCase(letterNotificationRequest.getPersonalisation().getRefundReason())) {

            Optional<List<Notification>> notificationList;
            notificationList = notificationRepository.findByReferenceAndNotificationTypeOrderByDateUpdatedDesc(
                letterNotificationRequest.getReference(), LETTER);

            if (notificationList.isPresent() && !notificationList.get().isEmpty()) {
                Notification notification = notificationList.get().stream().findAny().get();
                recipientPostalAddress = RecipientPostalAddress.recipientPostalAddressWith()
                    .addressLine(notification.getContactDetails().getAddressLine())
                    .city(notification.getContactDetails().getCity())
                    .county(notification.getContactDetails().getCountry())
                    .country(notification.getContactDetails().getCounty())
                    .postalCode(notification.getContactDetails().getPostcode())
                    .build();
                letterNotificationRequest.setRecipientPostalAddress(recipientPostalAddress);
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

            TemplatePreviewDto templatePreviewDto = letterNotificationRequest.getTemplatePreview();

            if (templatePreviewDto == null) {
                TemplatePreview templatePreview = notificationLetterClient
                    .generateTemplatePreview(
                        letterNotificationRequest.getTemplateId(),
                        createLetterPersonalisation(getRecipientContactAddressForRefundWhenContacted(letterNotificationRequest),letterNotificationRequest.getPersonalisation(),serviceContact.get().getServiceMailbox(),
                                                    letterNotificationRequest.getPersonalisation().getRefundReference(),
                                                    letterNotificationRequest.getPersonalisation().getCcdCaseNumber())
                    );
                templatePreviewDto = buildTemplatePreviewDTO(templatePreview, LETTER);
            }

            SendLetterResponse sendLetterResponse = notificationLetterClient.sendLetter(
                letterNotificationRequest.getTemplateId(),
                createLetterPersonalisation(getRecipientContactAddressForRefundWhenContacted(letterNotificationRequest),letterNotificationRequest.getPersonalisation(),serviceContact.get().getServiceMailbox(),
                                            letterNotificationRequest.getPersonalisation().getRefundReference(),
                                            letterNotificationRequest.getPersonalisation().getCcdCaseNumber()),
                letterNotificationRequest.getReference()
            );

            Notification notification = letterNotificationMapper.letterResponseMapper(
                sendLetterResponse,
                letterNotificationRequest,
                uid
            );
            notification.setTemplatePreview(templatePreviewDto);
            notificationRepository.save(notification);
            return sendLetterResponse;
        }catch (NotificationClientException exception){
            GovNotifyExceptionWrapper exceptionWrapper = new GovNotifyExceptionWrapper();
            LOG.error(exception.getMessage());
            throw exceptionWrapper.mapGovNotifyLetterException(exception);
        }
    }

    private Map<String, Object> createEmailPersonalisation(Personalisation personalisation, String serviceMailBox,
                                                           String refundRef, String ccdCaseNumber) {

        return Map.of("refundReference", refundRef,
                      "ccdCaseNumber", ccdCaseNumber,
                      "serviceMailbox", serviceMailBox,
                      "refundAmount", personalisation.getRefundAmount(),
                      "reason", personalisation.getRefundReason());
    }

    private Map<String, Object> createLetterPersonalisation(RecipientPostalAddress recipientPostalAddress, Personalisation personalisation, String serviceMailBox,
                                                            String refundRef, String ccdCaseNumber) {

        return Map.of("address_line_1", recipientPostalAddress.getAddressLine(),
                      "address_line_2", recipientPostalAddress.getCity(),
                      "address_line_3",recipientPostalAddress.getCounty(),
                      "address_line_4",recipientPostalAddress.getCountry(),
                      "address_line_5", recipientPostalAddress.getPostalCode(),
                      "refundReference", refundRef,
                      "ccdCaseNumber", ccdCaseNumber,
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
    public NotificationTemplatePreviewResponse previewNotification(DocPreviewRequest docPreviewRequest, MultiValueMap<String, String> headers) {
        TemplatePreview templatePreview;
        NotificationTemplatePreviewResponse notificationTemplatePreviewResponse;
        String instructionType ;
        PaymentDto paymentResponse;
        Optional<ServiceContact> serviceContact;
        String refundRef = "RF-****-****-****-****";
        String ccdCaseNumber;

        if (null == docPreviewRequest.getPaymentChannel()
            || docPreviewRequest.getPaymentChannel().equalsIgnoreCase(STRING)
            || null == docPreviewRequest.getPaymentMethod()
            || docPreviewRequest.getPaymentMethod().equalsIgnoreCase(STRING) ) {
            validateIfPaymentReferenceExist(docPreviewRequest);
            paymentResponse = fetchPaymentGroupResponse(headers,docPreviewRequest.getPaymentReference());
            instructionType = getInstructionType(paymentResponse.getChannel(),paymentResponse.getMethod());
            serviceContact = serviceContactRepository.findByServiceName(paymentResponse.getServiceName());
            ccdCaseNumber = paymentResponse.getCcdCaseNumber();
        } else {
            validateIfPaymentReferenceNotExist(docPreviewRequest);
            instructionType = getInstructionType(docPreviewRequest.getPaymentChannel(),docPreviewRequest.getPaymentMethod());
            serviceContact = serviceContactRepository.findByServiceName(docPreviewRequest.getServiceName());
            ccdCaseNumber = docPreviewRequest.getPersonalisation().getCcdCaseNumber();
        }

        String templateId = getTemplate(docPreviewRequest, instructionType);

        try {

            if(EMAIL.equalsIgnoreCase(docPreviewRequest.getNotificationType().name())) {
                LOG.info("EMAIL templateId {}", templateId);
                LOG.info("EMAIl Service Mailbox {}",serviceContact.get().getServiceMailbox());
                templatePreview = notificationEmailClient
                    .generateTemplatePreview(templateId,
                                             createEmailPersonalisation(docPreviewRequest.getPersonalisation(), serviceContact.get().getServiceMailbox(),
                                                                        refundRef, ccdCaseNumber));
                LOG.info("EMAIL templatePreview {}", templatePreview);
            } else {

                templatePreview = notificationLetterClient
                    .generateTemplatePreview(templateId,
                                             createLetterPersonalisation(docPreviewRequest.getRecipientPostalAddress(),docPreviewRequest.getPersonalisation(), serviceContact.get().getServiceMailbox(),
                                                                         refundRef,  ccdCaseNumber));
            }

         notificationTemplatePreviewResponse = notificationTemplateResponseMapper.notificationPreviewResponse(templatePreview,docPreviewRequest);
            LOG.info("notificationTemplatePreviewResponse  {}", notificationTemplatePreviewResponse.getTemplateId());
        } catch (NotificationClientException exception) {
            exception.printStackTrace();
            GovNotifyExceptionWrapper exceptionWrapper = new GovNotifyExceptionWrapper();
            LOG.error("Exception while sending Notification {}",exception.getMessage());
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

    private TemplatePreviewDto buildTemplatePreviewDTO(TemplatePreview templatePreview, String notificationType) {

        final String objNull = null;

        return TemplatePreviewDto.templatePreviewDtoWith()
            .id(templatePreview.getId())
            .templateType(templatePreview.getTemplateType())
            .version(templatePreview.getVersion())
            .body(templatePreview.getBody())
            .subject(templatePreview.getSubject().isPresent() ? templatePreview.getSubject().get() : objNull)
            .html(templatePreview.getHtml().isPresent() ? templatePreview.getHtml().get() : objNull)
            .from(notificationTemplateResponseMapper.toFromMapper(notificationType))
            .build();
    }

    private PaymentDto fetchPaymentGroupResponse(MultiValueMap<String, String> headers,
                                                          String paymentReference) {
        ResponseEntity<PaymentDto> paymentResponse = null;
        try {
            paymentResponse = fetchPaymentDataFromPayhub(headers, paymentReference);

        } catch (HttpClientErrorException e) {
            LOG.error(e.getMessage());
            if (e.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
                throw new PaymentReferenceNotFoundException("Payment Reference not found", e);
            }
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw new PaymentServerException("Payment Server Exception", e);
        }
        return paymentResponse.getBody();
    }

    private ResponseEntity<PaymentDto> fetchPaymentDataFromPayhub(MultiValueMap<String, String> headers,

                                                                                 String paymentReference) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(
            new StringBuilder(paymentApiUrl).append("/payments/").append(paymentReference)
                .toString());
         LOG.info("URI {}", builder.toUriString());
        return restTemplatePayment
            .exchange(
                builder.toUriString(),
                HttpMethod.GET,
                getHeadersEntity(headers), PaymentDto.class);
    }

    private HttpEntity<String> getHeadersEntity(MultiValueMap<String, String> headers) {
        return new HttpEntity<>(getFormatedHeaders(headers));
    }

    private MultiValueMap<String, String> getFormatedHeaders(MultiValueMap<String, String> headers) {
        List<String> authtoken = headers.get("authorization");
        List<String> servauthtoken = Arrays.asList(authTokenGenerator.generate());
        MultiValueMap<String, String> inputHeaders = new LinkedMultiValueMap<>();
        inputHeaders.put(CONTENT_TYPE, headers.get(CONTENT_TYPE));
        inputHeaders.put("Authorization", authtoken);
        inputHeaders.put("ServiceAuthorization", servauthtoken);
        return inputHeaders;
    }

    private String getInstructionType(String paymentChannel, String paymentMethod) {

        String instructionType;
        if (BULK_SCAN.equals(paymentChannel) && (CASH.equals(paymentMethod)
            || POSTAL_ORDER.equals(paymentMethod))) {
            instructionType = REFUND_WHEN_CONTACTED;
        } else {
            instructionType = SEND_REFUND;
        }

        return instructionType;
    }

    private void validateIfPaymentReferenceExist(DocPreviewRequest docPreviewRequest){

        if(null == docPreviewRequest.getPaymentReference() || docPreviewRequest.getPaymentReference().equalsIgnoreCase(STRING)){

            throw new DocPreviewBadRequestException("Payment reference cannot be null");
        }
    }

    private void validateIfPaymentReferenceNotExist(DocPreviewRequest docPreviewRequest) {

        if (null == docPreviewRequest.getPaymentChannel() || docPreviewRequest.getPaymentChannel()
            .equalsIgnoreCase(STRING) ||
            null == docPreviewRequest.getPaymentMethod() || docPreviewRequest.getPaymentMethod()
            .equalsIgnoreCase(STRING) ||
            null == docPreviewRequest.getServiceName() || docPreviewRequest.getServiceName()
            .equalsIgnoreCase(STRING)) {

            throw new DocPreviewBadRequestException("Payment channel, payment method, service name  cannot be null");

        }
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

    private boolean validAddress(RecipientPostalAddress address) {
        return !( StringUtils.isBlank(address.getPostalCode()) || StringUtils.isBlank(address.getAddressLine())
            || StringUtils.isBlank(address.getCity()) || StringUtils.isBlank(address.getCountry())
            || StringUtils.isBlank(address.getCounty()) );
    }
}
