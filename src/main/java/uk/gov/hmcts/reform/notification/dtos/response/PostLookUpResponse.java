package uk.gov.hmcts.reform.notification.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PostLookUpResponse {

    private PostCodeResponse data;
}
