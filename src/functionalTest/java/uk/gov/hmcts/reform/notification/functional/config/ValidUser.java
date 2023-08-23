package uk.gov.hmcts.reform.notification.functional.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder(builderMethodName = "userWith")
public class ValidUser {
    private final String email;
    private final String authorisationToken;
}
