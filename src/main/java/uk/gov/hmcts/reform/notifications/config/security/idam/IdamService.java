package uk.gov.hmcts.reform.notifications.config.security.idam;

import org.springframework.util.MultiValueMap;
import uk.gov.hmcts.reform.notifications.dtos.response.IdamUserIdResponse;

public interface IdamService {

    IdamUserIdResponse getUserId(MultiValueMap<String, String> headers);

}
