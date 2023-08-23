package uk.gov.hmcts.reform.notification.dtos.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder(builderMethodName = "buildNotificationWith")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class NotificationDto {

    private String reference;
    private String notificationType;
    private Date dateCreated;

    private Date dateUpdated;
    private ContactDetailsDto contactDetails;

    private NotificationTemplatePreviewResponse sentNotification;

}

