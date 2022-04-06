package uk.gov.hmcts.reform.notifications.dtos.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder(builderMethodName = "buildContactDetailsWith")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ContactDetailsDto {

    private String addressLine;
    private String city;
    private String country;
    private String postalCode;
    private String email;
    private String county;

    private Date dateCreated;

    private Date dateUpdated;
}
