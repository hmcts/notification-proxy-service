package uk.gov.hmcts.reform.notification.config.security.idam;

import org.springframework.util.MultiValueMap;
import uk.gov.hmcts.reform.notification.dtos.response.IdamUserIdResponse;

public interface IdamService {

    IdamUserIdResponse getUserId(MultiValueMap<String, String> headers);

}
