package uk.gov.hmcts.reform.notifications.dtos.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
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
@Builder(builderMethodName = "docPreviewRequestWith")

public class DocPreviewRequest {

    private String paymentReference;

    @NotNull(message = "payment method cannot be null")
    @NotEmpty(message = "payment method cannot be blank")
    private String paymentMethod;
    @NotNull(message = "payment channel cannot be null")
    @NotEmpty(message = "payment channel cannot be blank")
    private String paymentChannel;
    @NotNull(message = "service cannot be null")
    @NotEmpty(message = "Service cannot be blank")
    private String serviceName;

    @Valid
    private RecipientPostalAddress recipientPostalAddress;
    private String recipientEmailAddress;

    @NotNull
    private NotificationType notificationType;

    @NotNull
    @Valid
    private Personalisation personalisation;

}
