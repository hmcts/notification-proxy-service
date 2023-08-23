package uk.gov.hmcts.reform.notification.dtos.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class SchedulerRequest {

    @NotNull(message = "Reference has to be present")
    @ToString.Exclude
    private String reference;

}
