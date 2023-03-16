package uk.gov.hmcts.reform.notifications.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PostCodeResponse {

    private HeaderDetails header;
    private List<PostCodeResult> results;
}
