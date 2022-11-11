package uk.gov.hmcts.reform.notifications.dtos.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder(builderMethodName = "refundNotificationEmailRequestWith")
public class TempletePreviewRequest {

    @NotNull(message = "Template ID cannot be null")
    @NotEmpty(message = "Template ID cannot be blank")
    private String templateId;
}
