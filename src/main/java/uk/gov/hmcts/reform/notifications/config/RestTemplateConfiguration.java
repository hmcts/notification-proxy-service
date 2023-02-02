package uk.gov.hmcts.reform.notifications.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfiguration {

    @Bean("restTemplateIdam")
    public RestTemplate restTemplateIdam() {
        return  new RestTemplate(new HttpComponentsClientHttpRequestFactory());
    }

    @Bean("restTemplatePostCodeLookUp")
    public RestTemplate restTemplatePostCodeLookUp() {
        return  new RestTemplate(new HttpComponentsClientHttpRequestFactory());
    }

}
