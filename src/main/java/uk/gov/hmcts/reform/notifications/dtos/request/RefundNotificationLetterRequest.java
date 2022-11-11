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
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;


@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder(builderMethodName = "refundNotificationLetterRequestWith")
public class RefundNotificationLetterRequest {

    @NotNull(message = "Template ID cannot be null")
    @NotEmpty(message = "Template ID cannot be blank")
    private String templateId;

    @NotNull(message = "Reference cannot be null")
    @NotEmpty(message = "Reference cannot be blank")
    private String reference;

    @ApiModelProperty(example = "LETTER")
    @Value("LETTER")
    private NotificationType notificationType;

    @NotNull
    @Valid
    private Personalisation personalisation;

    @NotNull
    @Valid
    private RecipientPostalAddress recipientPostalAddress;

    private String ServiceName;

}
