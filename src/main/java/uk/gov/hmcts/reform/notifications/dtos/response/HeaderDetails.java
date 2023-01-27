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

    @JsonProperty("uri")
    private String uri;
    @JsonProperty("offset")
    private String offset;
    @JsonProperty("format")
    private String format;
    @JsonProperty("dataset")
    private String dataset;
    @JsonProperty("lr")
    private String lr;

    @JsonProperty("epoch")
    private String epoch;
    @JsonProperty("lastupdate")
    private String lastupdate;
    @JsonProperty("output_srs")
    private String outputSrs;
}
