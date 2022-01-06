package uk.gov.hmcts.reform.notifications.dtos.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.reform.notifications.dtos.enums.NotificationType;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder(builderMethodName = "refundNotificationEmailRequestWith")
public class RefundNotificationEmailRequest {

    @NotNull
    private String templateId;

    @NotNull
    @Email
    private String recipientEmailAddress;

    @NotNull
    private String reference;

    private String emailReplyToId;

    @ApiModelProperty(example = "EMAIL")
    private NotificationType notificationType;

    @NotNull
    private Personalisation personalisation;

}
