package uk.gov.hmcts.reform.notifications.dtos.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@Setter
@Getter
@NoArgsConstructor
public class PostCodeLookerResponse {
    @JsonProperty("results")
    private List<JsonNode> results;
}
