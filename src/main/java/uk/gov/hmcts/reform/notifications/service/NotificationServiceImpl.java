package uk.gov.hmcts.reform.notifications.service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
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
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.notifications.config.security.idam.IdamService;
import uk.gov.hmcts.reform.notifications.controllers.ExceptionHandlers;
import uk.gov.hmcts.reform.notifications.dtos.request.DocPreviewRequest;
import uk.gov.hmcts.reform.notifications.dtos.request.Personalisation;
import uk.gov.hmcts.reform.notifications.dtos.request.RecipientPostalAddress;
import uk.gov.hmcts.reform.notifications.dtos.request.RefundNotificationEmailRequest;
import uk.gov.hmcts.reform.notifications.dtos.request.RefundNotificationLetterRequest;
import uk.gov.hmcts.reform.notifications.dtos.response.IdamUserIdResponse;
import uk.gov.hmcts.reform.notifications.dtos.response.NotificationResponseDto;
import uk.gov.hmcts.reform.notifications.dtos.response.NotificationTemplatePreviewResponse;
import uk.gov.hmcts.reform.notifications.dtos.response.PaymentDto;
import uk.gov.hmcts.reform.notifications.exceptions.NotificationListEmptyException;
import uk.gov.hmcts.reform.notifications.exceptions.PaymentReferenceNotFoundException;
import uk.gov.hmcts.reform.notifications.exceptions.PaymentServerException;
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

import java.util.Map;
@Service
@SuppressWarnings({"PMD.TooManyFields", "PMD.ExcessiveImports"})
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
            Optional<ServiceContact> serviceContact = serviceContactRepository.findByServiceName(emailNotificationRequest.getServiceName());
            IdamUserIdResponse uid = idamService.getUserId(headers);
            SendEmailResponse sendEmailResponse = notificationEmailClient
                .sendEmail(
                    emailNotificationRequest.getTemplateId(),
                    emailNotificationRequest.getRecipientEmailAddress(),
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

    @Override
    public SendLetterResponse sendLetterNotification(RefundNotificationLetterRequest letterNotificationRequest, MultiValueMap<String, String> headers) {

        try {
            Optional<ServiceContact> serviceContact = serviceContactRepository.findByServiceName(letterNotificationRequest.getServiceName());
            IdamUserIdResponse uid = idamService.getUserId(headers);
            SendLetterResponse sendLetterResponse = notificationLetterClient.sendLetter(
                letterNotificationRequest.getTemplateId(),
                createLetterPersonalisation(letterNotificationRequest.getRecipientPostalAddress(),letterNotificationRequest.getPersonalisation(),serviceContact.get().getServiceMailbox(),
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
    public NotificationTemplatePreviewResponse previewNotification(DocPreviewRequest docPreviewRequest, MultiValueMap<String, String> headers) {
        TemplatePreview templatePreview;
        NotificationTemplatePreviewResponse notificationTemplatePreviewResponse;
        String instructionType ;
        PaymentDto paymentResponse;
        Optional<ServiceContact> serviceContact;
        String refundRef = "RF-****-****-****-****";

        if (null == docPreviewRequest.getPaymentChannel() || docPreviewRequest.getPaymentChannel().equalsIgnoreCase("string") || null == docPreviewRequest.getPaymentMethod() || docPreviewRequest.getPaymentMethod().equalsIgnoreCase("string") ) {

            paymentResponse = fetchPaymentGroupResponse(headers,docPreviewRequest.getPaymentReference());
            instructionType = getInstructionType(paymentResponse.getChannel(),paymentResponse.getMethod());
            serviceContact = serviceContactRepository.findByServiceName(paymentResponse.getServiceName());
        }
        else {

            instructionType = getInstructionType(docPreviewRequest.getPaymentChannel(),docPreviewRequest.getPaymentMethod());
            serviceContact = serviceContactRepository.findByServiceName(docPreviewRequest.getServiceName());
        }

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

    private PaymentDto fetchPaymentGroupResponse(MultiValueMap<String, String> headers,
                                                          String paymentReference) {
        ResponseEntity<PaymentDto> paymentResponse = null;
        try {
            LOG.info("insidefetchPaymentGroupResponse paymentResponse");
            paymentResponse = fetchPaymentDataFromPayhub(headers, paymentReference);
            LOG.info("paymentResponse != null: {}", paymentResponse != null);

        } catch (HttpClientErrorException e) {
            LOG.error(e.getMessage());
            if (e.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
                throw new PaymentReferenceNotFoundException("Payment Reference not found", e);
            }
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw new PaymentServerException("Payment Server Exception", e);
        }
        LOG.info("payment body {}", paymentResponse.getBody());
        return paymentResponse.getBody();
    }

    private ResponseEntity<PaymentDto> fetchPaymentDataFromPayhub(MultiValueMap<String, String> headers,

                                                                                 String paymentReference) {
        LOG.info("inside fetchPaymentDataFromPayhub");
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
        LOG.info("Authorization genrate1");
        List<String> servauthtoken = Arrays.asList(authTokenGenerator.generate());
        LOG.info("Authorization genrate2");
        MultiValueMap<String, String> inputHeaders = new LinkedMultiValueMap<>();
        inputHeaders.put(CONTENT_TYPE, headers.get(CONTENT_TYPE));
        inputHeaders.put("Authorization", authtoken);
        inputHeaders.put("ServiceAuthorization", servauthtoken);
        LOG.info("Authorization {}", inputHeaders.get("Authorization").get(0));
        LOG.info("ServiceAuthorization {}", inputHeaders.get("ServiceAuthorization").get(0));

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
}
