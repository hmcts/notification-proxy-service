package uk.gov.hmcts.reform.notifications.dtos.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.reform.notifications.dtos.enums.NotificationType;


@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder(builderMethodName = "refundNotificationLetterRequestWith")
public class RefundNotificationLetterRequest {

    private String templateId;

    private String reference;

    private NotificationType notificationType;

    private Personalisation personalisation;

    private RecipientPostalAddress recipientPostalAddress;

}
