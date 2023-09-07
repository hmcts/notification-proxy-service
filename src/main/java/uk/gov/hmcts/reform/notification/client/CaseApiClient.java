package uk.gov.hmcts.reform.notification.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.reform.notification.dtos.response.NotificationResponse;

@FeignClient(name = "case-api", url = "${case_api.url}")
public interface CaseApiClient {

    @PostMapping(value = "notification/case/{caseId}/update")
    ResponseEntity<Resource> updateNotificationDetails(@PathVariable final Long caseId,
                                                       @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
                                                       @RequestBody final NotificationResponse notificationResponse);
}
