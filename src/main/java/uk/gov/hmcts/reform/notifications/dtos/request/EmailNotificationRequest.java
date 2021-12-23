package uk.gov.hmcts.reform.notifications.dtos.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;


import javax.validation.Valid;
import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@With
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonInclude(NON_NULL)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmailNotificationRequest {

    @NotNull(message = "Name cannot be null")
    @NotEmpty(message = "Name cannot be blank")
    @Email
    private String email;

    @NotNull(message = "Name cannot be null")
    @NotEmpty(message = "Name cannot be blank")
    private String name;

    @NotNull(message = "Refund Reference cannot be null")
    @NotEmpty(message = "Refund Reference cannot be blank")
    private String refundReference;

}
