package uk.gov.hmcts.reform.notifications.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@Setter
@Getter
@NoArgsConstructor
public class ErrorResponse {
    private String message;
    private List<String> details;
}
