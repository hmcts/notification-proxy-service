package uk.gov.hmcts.reform.notification.dtos.response;


import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder(builderMethodName = "buildNotificationTemplatePreviewWith")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class NotificationTemplatePreviewResponse {

    private String templateId;
    private String templateType;
    private FromTemplateContact from;
    private String subject;
    private String html;
    private RecipientContact recipientContact;
    private String body;

}
