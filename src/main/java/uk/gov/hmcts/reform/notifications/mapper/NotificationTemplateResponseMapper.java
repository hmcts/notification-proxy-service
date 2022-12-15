package uk.gov.hmcts.reform.notifications.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.notifications.dtos.request.DocPreviewRequest;
import uk.gov.hmcts.reform.notifications.dtos.response.FromTemplateContact;
import uk.gov.hmcts.reform.notifications.dtos.response.NotificationTemplatePreviewResponse;
import uk.gov.hmcts.reform.notifications.dtos.response.RecipientContact;
import uk.gov.hmcts.reform.notifications.dtos.response.MailAddress;
import uk.gov.hmcts.reform.notifications.model.ServiceContact;
import uk.gov.service.notify.TemplatePreview;

@Component
public class NotificationTemplateResponseMapper {

    private static final String EMAIL = "EMAIL";
    private static final String LETTER = "LETTER";

    public NotificationTemplatePreviewResponse notificationPreviewResponse(TemplatePreview templatePreview,
                                                                           DocPreviewRequest docPreviewRequest,
                                                                           ServiceContact serviceContact){

        return NotificationTemplatePreviewResponse.buildNotificationTemplatePreviewWith()
            .templateId(templatePreview.getId().toString())
            .templateType(templatePreview.getTemplateType())
            .from(toFromMapper(docPreviewRequest.getNotificationType().name(), serviceContact))
            .html(toHtmlMapper(templatePreview))
            .recipientContact(toContactMapper(docPreviewRequest))
            .subject(templatePreview.getSubject().get())
            .body(templatePreview.getBody())
            .build();
    }

    private String toHtmlMapper(TemplatePreview templatePreview){

        String html = null;
        if(templatePreview.getHtml().isPresent()){
            html = templatePreview.getHtml().get();
        }
        return html;
    }

    private RecipientContact toContactMapper(DocPreviewRequest docPreviewRequest) {

        return RecipientContact.buildRecipientContactWith()
            .recipientEmailAddress(toEmailMapper(docPreviewRequest))
            .recipientMailAddress(toMailMapper(docPreviewRequest))
            .build();
    }

    private String toEmailMapper(DocPreviewRequest docPreviewReques) {

        String email = null;
        if(EMAIL.equalsIgnoreCase(docPreviewReques.getNotificationType().name())) {

            email = docPreviewReques.getRecipientEmailAddress();

        }

        return email;
    }

    private MailAddress toMailMapper(DocPreviewRequest docPreviewReques) {

        MailAddress recipientMailAddress = null;
        if(LETTER.equalsIgnoreCase(docPreviewReques.getNotificationType().name())) {

            recipientMailAddress = MailAddress.buildRecipientMailAddressWith()
                .addressLine(docPreviewReques.getRecipientPostalAddress().getAddressLine())
                .city(docPreviewReques.getRecipientPostalAddress().getCity())
                .county(docPreviewReques.getRecipientPostalAddress().getCounty())
                .country(docPreviewReques.getRecipientPostalAddress().getCountry())
                .postalCode(docPreviewReques.getRecipientPostalAddress().getPostalCode())
                .build();
        }

        return recipientMailAddress;
    }

    public FromTemplateContact toFromMapper(String notificationType, ServiceContact serviceContact) {

        return FromTemplateContact.buildFromTemplateContactWith()
            .fromEmailAddress(toFromEmailMapper(notificationType, serviceContact))
            .fromMailAddress(toFromMailMapper(notificationType, serviceContact))
            .build();
    }

    private String toFromEmailMapper(String notificationType, ServiceContact serviceContact) {

        String email = null;
        if(EMAIL.equalsIgnoreCase(notificationType)) {
           email = serviceContact.getFromEmailAddress();
        }
        return email;
    }

    private MailAddress toFromMailMapper(String notificationType, ServiceContact serviceContact) {

        MailAddress fromMailAddress = null;
        if(LETTER.equalsIgnoreCase(notificationType)) {

            fromMailAddress = MailAddress.buildRecipientMailAddressWith()
                .addressLine(serviceContact.getFromMailAddress().getAddressLine())
                .city(serviceContact.getFromMailAddress().getCity())
                .county(serviceContact.getFromMailAddress().getCounty())
                .country(serviceContact.getFromMailAddress().getCountry())
                .postalCode(serviceContact.getFromMailAddress().getPostalCode())
                .build();
        }
        return fromMailAddress;
    }

}

