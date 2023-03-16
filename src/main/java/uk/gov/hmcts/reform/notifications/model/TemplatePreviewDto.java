package uk.gov.hmcts.reform.notifications.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;
import uk.gov.hmcts.reform.notifications.dtos.response.FromTemplateContact;

import java.util.UUID;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;


@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@AllArgsConstructor
@NoArgsConstructor
@Builder(builderMethodName = "templatePreviewDtoWith")
@JsonInclude(NON_NULL)
@Setter
@Getter
public class TemplatePreviewDto {

    private UUID id;

    private String templateType;

    private int version;

    private String body;

    private String subject;

    private String html;

    private FromTemplateContact from;
}
