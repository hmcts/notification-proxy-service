package uk.gov.hmcts.reform.notifications.dtos.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PostCodeResult {

    @JsonProperty("DPA")
    private AddressDetails dpa;
}
