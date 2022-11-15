package uk.gov.hmcts.reform.notifications.mapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.notifications.dtos.request.DocPreviewRequest;
import uk.gov.hmcts.reform.notifications.dtos.response.FromTemplateContact;
import uk.gov.hmcts.reform.notifications.dtos.response.NotificationTemplatePreviewResponse;
import uk.gov.hmcts.reform.notifications.dtos.response.RecipientContact;
import uk.gov.hmcts.reform.notifications.dtos.response.MailAddress;
import uk.gov.service.notify.TemplatePreview;

@Component
public class NotificationTemplateResponseMapper {

    @Value("${notify.template.from-details.email}")
    private String notifyTemplateFromEmail;

    @Value("${notify.template.from-details.mail.address-line}")
    private String notifyTemplateFromMailAddressLine;

    @Value("${notify.template.from-details.mail.county}")
    private String notifyTemplateFromMailCounty;

    @Value("${notify.template.from-details.mail.country}")
    private String notifyTemplateFromMailCountry;

    @Value("${notify.template.from-details.mail.city}")
    private String notifyTemplateFromMailCity;

    @Value("${notify.template.from-details.mail.post-code}")
    private String notifyTemplateFromMailPostCode;

    private static final String EMAIL = "EMAIL";
    private static final String LETTER = "LETTER";

    public NotificationTemplatePreviewResponse notificationPreviewResponse(TemplatePreview templatePreview, DocPreviewRequest docPreviewRequest){

        return NotificationTemplatePreviewResponse.buildNotificationTemplatePreviewWith()
            .templateId(templatePreview.getId().toString())
            .templateType(templatePreview.getTemplateType())
            .from(toFromMapper(docPreviewRequest))
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

    private FromTemplateContact toFromMapper(DocPreviewRequest docPreviewRequest) {

        return FromTemplateContact.buildFromTemplateContactWith()
            .fromEmailAddress(toFromEmailMapper(docPreviewRequest))
            .fromMailAddress(toFromMailMapper(docPreviewRequest))
            .build();
    }

    private String toFromEmailMapper(DocPreviewRequest docPreviewReques) {

        String email = null;
        if(EMAIL.equalsIgnoreCase(docPreviewReques.getNotificationType().name())) {

            email = notifyTemplateFromEmail;

        }

        return email;
    }

    private MailAddress toFromMailMapper(DocPreviewRequest docPreviewReques) {

        MailAddress fromMailAddress = null;
        if(LETTER.equalsIgnoreCase(docPreviewReques.getNotificationType().name())) {

            fromMailAddress = MailAddress.buildRecipientMailAddressWith()
                .addressLine(notifyTemplateFromMailAddressLine)
                .city(notifyTemplateFromMailCity)
                .county(notifyTemplateFromMailCounty)
                .country(notifyTemplateFromMailCountry)
                .postalCode(notifyTemplateFromMailPostCode)
                .build();
        }

        return fromMailAddress;
    }

}

