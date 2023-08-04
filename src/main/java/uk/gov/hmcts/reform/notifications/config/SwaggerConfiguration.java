package uk.gov.hmcts.reform.notifications.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.media.StringSchema;
import org.springdoc.core.GroupedOpenApi;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfiguration {

    private static final String HEADER = "header";

    @Bean
    public GroupedOpenApi paymentBarReferenceDataApi() {
        return GroupedOpenApi.builder()
            .group("notifications")
            .packagesToScan("uk.gov.hmcts.reform.notifications.controllers")
            .pathsToMatch("/**")
            .addOperationCustomizer(authorizationHeaders())
            .build();
    }

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI().components(new Components())
            .info(new Info().title("Notification Service documentation").version("1.0.0"));
    }

    @Bean
    public OperationCustomizer authorizationHeaders() {
        return (operation, handlerMethod) ->
            operation
                .addParametersItem(
                    mandatoryStringParameter("Authorization", "User authorization header"))
                .addParametersItem(
                    mandatoryStringParameter("ServiceAuthorization", "Service authorization header"));
    }

    private Parameter mandatoryStringParameter(String name, String description) {
        return new Parameter()
            .name(name)
            .description(description)
            .required(true)
            .in(HEADER)
            .schema(new StringSchema());
    }

}
