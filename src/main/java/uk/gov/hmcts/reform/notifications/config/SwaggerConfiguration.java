package uk.gov.hmcts.reform.notifications.config;

import java.util.Arrays;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;
import com.google.common.base.Predicate;
import springfox.documentation.builders.ParameterBuilder;

import springfox.documentation.RequestHandler;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.Parameter;


@Configuration
@EnableSwagger2
public class SwaggerConfiguration {

/*
This needs to be uncommented when auth tokens are required to access the endpoints
 */

    private List<Parameter> getGlobalOperationParameters() {
        return Arrays.asList(
            new ParameterBuilder()
                .name("Authorization")
                .description("User authorization header")
                .required(false)
                .parameterType("header")
                .modelRef(new ModelRef("string"))
                .build(),
            new ParameterBuilder()
                .name("ServiceAuthorization")
                .description("Service authorization header")
                .required(true)
                .parameterType("header")
                .modelRef(new ModelRef("string"))
                .build());
    }


    @Bean
    public Docket notificationsApi() {
        return new Docket(DocumentationType.SWAGGER_2)
            .groupName("notifications")
            .globalOperationParameters(getGlobalOperationParameters())
            .useDefaultResponseMessages(false)
            .apiInfo(paymentApiInfo())
            .select()
            .apis(packagesLike("uk.gov.hmcts.reform.notifications.controllers"))
            .paths(PathSelectors.any())
            .build();
    }

    private ApiInfo paymentApiInfo() {
        return new ApiInfoBuilder()
            .title("Notifications Service API documentation")
            .description("Notifications Service documentation")
            .build();
    }

    private static Predicate<RequestHandler> packagesLike(final String pkg) {
        return input -> input.declaringClass().getPackage().getName().equals(pkg);
    }

}
