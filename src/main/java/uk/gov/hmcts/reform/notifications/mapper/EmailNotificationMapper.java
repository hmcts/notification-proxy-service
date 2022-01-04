package uk.gov.hmcts.reform.notifications.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.notifications.dtos.request.RefundNotificationEmailRequest;
import uk.gov.hmcts.reform.notifications.model.ContactDetails;
import uk.gov.hmcts.reform.notifications.model.Notification;
import uk.gov.service.notify.SendEmailResponse;

import java.util.ArrayList;
import java.util.List;

@Component
public class EmailNotificationMapper {

    public Notification emailResponseMapper(SendEmailResponse sendEmailResponse, RefundNotificationEmailRequest emailNotificationRequest) {
        List<ContactDetails> contactDetailsList = new ArrayList<>();
        contactDetailsList.add(ContactDetails.contactDetailsWith()
                                   .email(emailNotificationRequest.getRecipientEmailAddress())
                                   .createdBy("System")
                                   .build());
        return Notification.builder()
            .notificationType(emailNotificationRequest.getNotificationType().toString())
            .reference(sendEmailResponse.getReference().get())
            .templateId(sendEmailResponse.getTemplateId().toString())
            .createdBy("System")
            .contactDetails(contactDetailsList)
            .build();
    }

}
