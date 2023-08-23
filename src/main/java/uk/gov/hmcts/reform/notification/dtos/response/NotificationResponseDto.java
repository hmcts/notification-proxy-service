package uk.gov.hmcts.reform.notification.dtos.response;

import java.util.List;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

@Builder(builderMethodName = "buildNotificationListWith")
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class NotificationResponseDto {
    private List<NotificationDto> notifications;
}

