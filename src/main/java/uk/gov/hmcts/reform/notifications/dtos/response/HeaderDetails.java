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
public class HeaderDetails {

    @JsonProperty("query")
    private String query;

    @JsonProperty("maxresults")
    private int maxresults;

    @JsonProperty("totalresults")
    private int totalresults;
}
