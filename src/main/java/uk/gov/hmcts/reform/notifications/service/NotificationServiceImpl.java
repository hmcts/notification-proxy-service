package uk.gov.hmcts.reform.notifications.service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.hmcts.reform.notifications.config.PostcodeLookupConfiguration;
import uk.gov.hmcts.reform.notifications.config.security.idam.IdamService;
import uk.gov.hmcts.reform.notifications.dtos.request.*;
import uk.gov.hmcts.reform.notifications.dtos.response.*;
import uk.gov.hmcts.reform.notifications.exceptions.*;
import uk.gov.hmcts.reform.notifications.mapper.EmailNotificationMapper;
import uk.gov.hmcts.reform.notifications.mapper.LetterNotificationMapper;
import uk.gov.hmcts.reform.notifications.mapper.NotificationResponseMapper;
import uk.gov.hmcts.reform.notifications.mapper.NotificationTemplateResponseMapper;
import uk.gov.hmcts.reform.notifications.model.Notification;
import uk.gov.hmcts.reform.notifications.model.NotificationRefundReasons;
import uk.gov.hmcts.reform.notifications.model.ServiceContact;
import uk.gov.hmcts.reform.notifications.model.TemplatePreviewDto;
import uk.gov.hmcts.reform.notifications.repository.NotificationRefundReasonRepository;
import uk.gov.hmcts.reform.notifications.repository.NotificationRepository;
import uk.gov.hmcts.reform.notifications.repository.ServiceContactRepository;
import uk.gov.hmcts.reform.notifications.util.GovNotifyExceptionWrapper;
import uk.gov.service.notify.*;

@Service
@SuppressWarnings({"PMD.TooManyFields", "PMD.ExcessiveImports", "PMD.TooManyMethods", "PMD.GodClass","PMD.CyclomaticComplexity"})
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
    private NotificationRefundReasonRepository notificationRefundReasonRepository;

    @Autowired
    private NotificationTemplateResponseMapper notificationTemplateResponseMapper;

    @Autowired
    private PostcodeLookupConfiguration configuration;

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

    @Value("${notify.template.cheque-po-cash.letter}")
    private String chequePoCashLetterTemplateId;

    @Value("${notify.template.cheque-po-cash.email}")
    private String chequePoCashEmailTemplateId;

    @Value("${notify.template.card-pba.letter}")
    private String cardPbaLetterTemplateId;

    @Value("${notify.template.card-pba.email}")
    private String cardPbaEmailTemplateId;

    @Autowired()
    @Qualifier("restTemplatePostCodeLookUp")
    private RestTemplate restTemplatePostCodeLookUp;

    @Autowired
    ObjectMapper objectMapper;
    @Override
    public SendEmailResponse sendEmailNotification(RefundNotificationEmailRequest emailNotificationRequest, MultiValueMap<String, String> headers) {
        try {
            LOG.info("sendEmailNotification -->" +emailNotificationRequest.toString());

            Optional<ServiceContact> serviceContactOptional = serviceContactRepository.findByServiceName(emailNotificationRequest.getServiceName());
            ServiceContact serviceContact = new ServiceContact();

            if(serviceContactOptional.isPresent()){
                serviceContact = serviceContactOptional.get();
            }

            IdamUserIdResponse uid = idamService.getUserId(headers);
            LOG.info("Refund reason in sendEmailNotification {}", emailNotificationRequest.getPersonalisation().getRefundReason());
            String refundReason = getRefundReason(emailNotificationRequest.getPersonalisation().getRefundReason());
            TemplatePreviewDto templatePreviewDto = emailNotificationRequest.getTemplatePreview();
            LOG.info("templatePreviewDto {}",templatePreviewDto);
            if (templatePreviewDto == null) {
                TemplatePreview templatePreview = notificationEmailClient
                    .generateTemplatePreview(
                        emailNotificationRequest.getTemplateId(),
                        createEmailPersonalisation(emailNotificationRequest.getPersonalisation(), serviceContact.getServiceMailbox(),
                                                   emailNotificationRequest.getPersonalisation().getRefundReference(),
                                                   emailNotificationRequest.getPersonalisation().getCcdCaseNumber(),
                                                   refundReason)
                    );
                templatePreviewDto = buildTemplatePreviewDTO(templatePreview, EMAIL, serviceContact);
            }
            LOG.info("Before sending mail to Notification Client ");
            SendEmailResponse sendEmailResponse = notificationEmailClient
                .sendEmail(
                    emailNotificationRequest.getTemplateId(),
                    emailNotificationRequest.getRecipientEmailAddress(),
                    createEmailPersonalisation(emailNotificationRequest.getPersonalisation(), serviceContact.getServiceMailbox(),
                                               emailNotificationRequest.getPersonalisation().getRefundReference(),
                                               emailNotificationRequest.getPersonalisation().getCcdCaseNumber(),
                                               refundReason),
                    emailNotificationRequest.getReference()
                );
            LOG.info(" Notification Email sent to Client ");
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

    @Override
    public SendLetterResponse sendLetterNotification(RefundNotificationLetterRequest letterNotificationRequest, MultiValueMap<String, String> headers) {

        try {
            Optional<ServiceContact> serviceContactOptional = serviceContactRepository.findByServiceName(letterNotificationRequest.getServiceName());

            ServiceContact serviceContact = new ServiceContact();

            if(serviceContactOptional.isPresent()){
                serviceContact = serviceContactOptional.get();
            }

            IdamUserIdResponse uid = idamService.getUserId(headers);

            TemplatePreviewDto templatePreviewDto = letterNotificationRequest.getTemplatePreview();
            String refundReason = getRefundReason(letterNotificationRequest.getPersonalisation().getRefundReason());
            LOG.info("Refund Reason in sendLetterNotification {}",refundReason);
            if (templatePreviewDto == null) {
                TemplatePreview templatePreview = notificationLetterClient
                    .generateTemplatePreview(
                        letterNotificationRequest.getTemplateId(),
                        createLetterPersonalisation(letterNotificationRequest.getRecipientPostalAddress(),letterNotificationRequest.getPersonalisation(),serviceContact.getServiceMailbox(),
                                                    letterNotificationRequest.getPersonalisation().getRefundReference(),
                                                    letterNotificationRequest.getPersonalisation().getCcdCaseNumber(), refundReason)
                    );
                templatePreviewDto = buildTemplatePreviewDTO(templatePreview, LETTER, serviceContact);
            }
            LOG.info("templatePreviewDto {}",templatePreviewDto);
            SendLetterResponse sendLetterResponse = notificationLetterClient.sendLetter(
                letterNotificationRequest.getTemplateId(),
                createLetterPersonalisation(letterNotificationRequest.getRecipientPostalAddress(),letterNotificationRequest.getPersonalisation(),serviceContact.getServiceMailbox(),
                                            letterNotificationRequest.getPersonalisation().getRefundReference(),
                                            letterNotificationRequest.getPersonalisation().getCcdCaseNumber(),
                                            refundReason),
                letterNotificationRequest.getReference()
            );
            LOG.info("sendLetterResponse {}",sendLetterResponse.getBody());
            Notification notification = letterNotificationMapper.letterResponseMapper(
                sendLetterResponse,
                letterNotificationRequest,
                uid
            );
            LOG.info("notification {}",notification.getReference());
            notification.setTemplatePreview(templatePreviewDto);
            notificationRepository.save(notification);
            LOG.info("Letter notification saved successfully.");
            return sendLetterResponse;
        }catch (NotificationClientException exception){
            GovNotifyExceptionWrapper exceptionWrapper = new GovNotifyExceptionWrapper();
            LOG.error(exception.getMessage());
            throw exceptionWrapper.mapGovNotifyLetterException(exception);
        }
    }

    private Map<String, Object> createEmailPersonalisation(Personalisation personalisation, String serviceMailBox,
                                                           String refundRef, String ccdCaseNumber, String refundReason) {

        return Map.of("refundReference", refundRef,
                      "ccdCaseNumber", ccdCaseNumber,
                      "serviceMailbox", serviceMailBox,
                      "refundAmount", personalisation.getRefundAmount(),
                      "reason", refundReason);
    }

    private Map<String, Object> createLetterPersonalisation(RecipientPostalAddress recipientPostalAddress, Personalisation personalisation, String serviceMailBox,
                                                            String refundRef, String ccdCaseNumber, String refundReason ) {

        return Map.of("address_line_1", recipientPostalAddress.getAddressLine(),
                      "address_line_2", recipientPostalAddress.getCity(),
                      "address_line_3",recipientPostalAddress.getCounty(),
                      "address_line_4",recipientPostalAddress.getCountry(),
                      "address_line_5", recipientPostalAddress.getPostalCode(),
                      "refundReference", refundRef,
                      "ccdCaseNumber", ccdCaseNumber,
                      "serviceMailbox", serviceMailBox,
                      "refundAmount", personalisation.getRefundAmount(),
                      "reason", refundReason);
    }

    @Override
    public NotificationResponseDto getNotification(String reference) {
        Optional<List<Notification>> notificationList;
        notificationList = notificationRepository.findByReferenceOrderByDateUpdatedDesc(reference);
        LOG.info("Notification List retrieved in getNotification {}",notificationList);
        if (notificationList.isPresent() && !notificationList.get().isEmpty()) {

            notificationResponseDto = NotificationResponseDto
                .buildNotificationListWith()
                .notifications(notificationList.get().stream().map(notificationResponseMapper::notificationResponse)
                                   .collect(Collectors.toList()))
                .build();
            LOG.info("Notification Response prepared from getNotification {}",notificationResponseDto);
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
        String refundRef = getRefundReference(docPreviewRequest);
        LOG.info("Refund reference in previewNotification {}", refundRef);
        String refundReason = getRefundReason(docPreviewRequest.getPersonalisation().getRefundReason());
        LOG.info("Refund reason in previewNotification {}", refundReason);
        String ccdCaseNumber;
        instructionType = getInstructionType(docPreviewRequest.getPaymentChannel(),docPreviewRequest.getPaymentMethod());

        Optional<ServiceContact> serviceContactOptional = serviceContactRepository.findByServiceName(docPreviewRequest.getServiceName());
        ServiceContact serviceContact = new ServiceContact();

        if(serviceContactOptional.isPresent()){
            serviceContact = serviceContactOptional.get();
        }

        ccdCaseNumber = docPreviewRequest.getPersonalisation().getCcdCaseNumber();

        String templateId = getTemplate(docPreviewRequest, instructionType);
        try {
            if(EMAIL.equalsIgnoreCase(docPreviewRequest.getNotificationType().name())) {
                templatePreview = notificationEmailClient
                    .generateTemplatePreview(templateId,
                                             createEmailPersonalisation(docPreviewRequest.getPersonalisation(), serviceContact.getServiceMailbox(),
                                                                        refundRef, ccdCaseNumber,refundReason));
                LOG.info("EMAIL templatePreview {}", templatePreview);

            } else {

                templatePreview = notificationLetterClient
                    .generateTemplatePreview(templateId,
                                             createLetterPersonalisation(docPreviewRequest.getRecipientPostalAddress(),docPreviewRequest.getPersonalisation(), serviceContact.getServiceMailbox(),
                                                                         refundRef, ccdCaseNumber, refundReason));
                LOG.info("LETTER templatePreview {}", templatePreview);
            }

         notificationTemplatePreviewResponse = notificationTemplateResponseMapper.notificationPreviewResponse(templatePreview,
                                                                                                              docPreviewRequest,
                                                                                                              serviceContact);
        } catch (NotificationClientException exception) {
            LOG.error("NotificationServiceImpl.previewNotification() : {}", exception);
            GovNotifyExceptionWrapper exceptionWrapper = new GovNotifyExceptionWrapper();
            LOG.error("Exception while sending Notification {}",exception.getMessage());
            throw exceptionWrapper.mapGovNotifyPreviewException(exception);
        }
        return notificationTemplatePreviewResponse;
    }

    @Override
    @Transactional
    public void deleteNotification(String reference) {
        long records = notificationRepository.deleteByReference(reference);
        LOG.info("records After deletion {}",records);
        if (records == 0) {
            throw new NotificationNotFoundException("No records found for given refund reference");
        }
    }

    private  String getTemplate(DocPreviewRequest docPreviewRequest, String instructionType) {
        String templateId = null;

        LOG.info("getTemplate getTemplate {}", docPreviewRequest.getNotificationType().name());
        if (null != docPreviewRequest.getNotificationType().name()) {

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

    private TemplatePreviewDto buildTemplatePreviewDTO(TemplatePreview templatePreview, String notificationType,
                                                       ServiceContact serviceContact) {

         String subject = null;
         String html = null;

        Optional<String> subjectOptional = templatePreview.getSubject();
        if(subjectOptional.isPresent() && !subjectOptional.isEmpty()) {
            subject = subjectOptional.get();
        }

        Optional<String> htmlOptional = templatePreview.getHtml();
        if(htmlOptional.isPresent() && !htmlOptional.isEmpty()) {
            html = htmlOptional.get();
        }

        return TemplatePreviewDto.templatePreviewDtoWith()
            .id(templatePreview.getId())
            .templateType(templatePreview.getTemplateType())
            .version(templatePreview.getVersion())
            .body(templatePreview.getBody())
            .subject(subject)
            .html(html)
            .from(notificationTemplateResponseMapper.toFromMapper(notificationType, serviceContact))
            .build();
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

    private String getRefundReason(String refundReasonCode) {
        LOG.info("refundReasonCode >>  {}",refundReasonCode);
        final int refundreasoncodelimit = 5;
        String reasonCode;
            if (refundReasonCode.length() > refundreasoncodelimit) {
                reasonCode = refundReasonCode.split("-")[0];
            } else {
                reasonCode = refundReasonCode;
            }
            LOG.info("reasonCode for searching in Notifications Repo >>  {}", reasonCode);
            String refundReason;
        Optional<NotificationRefundReasons> notificationRefundReasons = notificationRefundReasonRepository.findByRefundReasonCode(reasonCode);

            if (notificationRefundReasons.isPresent()) {
                refundReason = notificationRefundReasons.get().getRefundReasonNotification();
            } else {
                throw new RefundReasonNotFoundException("Invalid Reason Type : " + refundReasonCode);
            }
            return refundReason;
    }

    private String getRefundReference(DocPreviewRequest docPreviewRequest) {

        String refundRef;
        if(null == docPreviewRequest.getPersonalisation().getRefundReference() || docPreviewRequest.getPersonalisation().getRefundReference().equalsIgnoreCase(STRING)
            || docPreviewRequest.getPersonalisation().getRefundReference().isEmpty()) {
            refundRef = "RF-****-****-****-****";
        } else {
            refundRef = docPreviewRequest.getPersonalisation().getRefundReference();
        }
          return refundRef;
    }
    @Override
    public PostCodeResponse getAddress(String postCode){

        PostCodeResponse results = null;
        try {
            ConcurrentHashMap<String, String> params = new ConcurrentHashMap<>();
            params.put("postcode", StringUtils.deleteWhitespace(postCode));
            String url = configuration.getUrl();
            String key = configuration.getAccessKey();
            params.put("key", key);
            if (null == url) {
                throw new PostCodeLookUpException("Postcode URL is null");
            }
            if (null == key || StringUtils.isEmpty(key)) {
                throw new PostCodeLookUpException("Postcode API Key is null");
            }
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url + "/postcode");
            for (Map.Entry<String, String> entry : params.entrySet()) {
                builder.queryParam(entry.getKey(), entry.getValue());
            }

            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/json");

            HttpEntity<String> response =
                restTemplatePostCodeLookUp.exchange(
                    builder.toUriString(),
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    String.class);

            HttpStatus responseStatus = ((ResponseEntity) response).getStatusCode();

            if (responseStatus.value() == org.apache.http.HttpStatus.SC_OK) {
                results = objectMapper.readValue(response.getBody(), PostCodeResponse.class);

                return results;
            } else if (responseStatus.value() == org.apache.http.HttpStatus.SC_NOT_FOUND) {
                throw new PostCodeLookUpNotFoundException("Postcode " + postCode + " not found");
            }
        } catch (Exception e) {
            LOG.error("Postcode Lookup Failed - ", e.getMessage());
            throw new PostCodeLookUpException(e.getMessage(), e);
        }

        return results;
    }
}
