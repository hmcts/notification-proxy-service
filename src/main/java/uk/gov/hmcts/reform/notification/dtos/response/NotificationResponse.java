package uk.gov.hmcts.reform.notification.dtos.response;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class NotificationResponse {
    private String templateId;
    private String reference;
}
