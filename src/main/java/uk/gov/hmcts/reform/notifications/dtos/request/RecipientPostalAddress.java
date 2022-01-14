package uk.gov.hmcts.reform.notifications.dtos.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder(builderMethodName = "recipientPostalAddressWith")
public class RecipientPostalAddress {

    @NotNull(message = "Address line cannot be null")
    @NotEmpty(message = "Address line cannot be blank")
    private String addressLine;

    @NotNull(message = "City cannot be null")
    @NotEmpty(message = "City cannot be blank")
    private String city;

    @NotNull(message = "County cannot be null")
    @NotEmpty(message = "County cannot be blank")
    private String county;

    @NotNull(message = "Country cannot be null")
    @NotEmpty(message = "Country cannot be blank")
    private String country;

    @NotNull(message = "Postal Code cannot be null")
    @NotEmpty(message = "Postal Code cannot be blank")
    private String postalCode;
}
