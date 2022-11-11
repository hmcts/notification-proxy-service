package uk.gov.hmcts.reform.notifications.dtos.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.reform.notifications.dtos.enums.NotificationType;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder(builderMethodName = "refundNotificationEmailRequestWith")
public class RefundNotificationEmailRequest {

    @NotNull(message = "Template ID cannot be null")
    @NotEmpty(message = "Template ID cannot be blank")
    private String templateId;

    @NotNull(message = "Recipient Email Address cannot be null")
    @NotEmpty(message = "Recipient Email Address cannot be blank")
    @Email(message = "Please enter a valid Email Address")
    private String recipientEmailAddress;

    @NotNull(message = "Reference cannot be null")
    @NotEmpty(message = "Reference cannot be blank")
    private String reference;

    private String emailReplyToId;

    @ApiModelProperty(example = "EMAIL")
    @Value("EMAIL")
    private NotificationType notificationType;

    @NotNull
    @Valid
    private Personalisation personalisation;

    private String serviceName;

}
