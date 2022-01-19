package uk.gov.hmcts.reform.notifications.config.security;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationFilter;
import uk.gov.hmcts.reform.authorisation.filters.ServiceAuthFilter;
import uk.gov.hmcts.reform.notifications.config.security.converter.NotificationsJwtGrantedAuthoritiesConverter;
import uk.gov.hmcts.reform.notifications.config.security.exception.NotificationsAccessDeniedHandler;
import uk.gov.hmcts.reform.notifications.config.security.exception.NotificationsAuthenticationEntryPoint;
import uk.gov.hmcts.reform.notifications.config.security.filiters.ServiceAndUserAuthFilter;
import uk.gov.hmcts.reform.notifications.config.security.utils.SecurityUtils;
import uk.gov.hmcts.reform.notifications.config.security.validator.AudienceValidator;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;


@EnableWebSecurity
public class SpringSecurityConfiguration {

    @Configuration
    public static class InternalApiSecurityConfigurationAdapter extends WebSecurityConfigurerAdapter {

        private static final Logger LOG = LoggerFactory.getLogger(SpringSecurityConfiguration.class);
        private final ServiceAuthFilter serviceAuthFilter;
        private final ServiceAndUserAuthFilter serviceAndUserAuthFilter;
        private final JwtAuthenticationConverter jwtAuthenticationConverter;
        private final NotificationsAuthenticationEntryPoint notificationsAuthenticationEntryPoint;
        private final NotificationsAccessDeniedHandler notificationsAccessDeniedHandler;
        @Value("${spring.security.oauth2.client.provider.oidc.issuer-uri}")
        private String issuerUri;
        @Value("${oidc.audience-list}")
        private String[] allowedAudiences;

        @Inject
        public InternalApiSecurityConfigurationAdapter(final NotificationsJwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter,
                                                       final ServiceAuthFilter serviceAuthFilter,
                                                       final Function<HttpServletRequest, Optional<String>> userIdExtractor,
                                                       final SecurityUtils securityUtils,
                                                       final NotificationsAuthenticationEntryPoint notificationsAuthenticationEntryPoint,
                                                       final NotificationsAccessDeniedHandler notificationsAccessDeniedHandler) {
            super();
            this.serviceAndUserAuthFilter = new ServiceAndUserAuthFilter(
                userIdExtractor, securityUtils);
            this.serviceAuthFilter = serviceAuthFilter;
            jwtAuthenticationConverter = new JwtAuthenticationConverter();
            jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);
            this.notificationsAuthenticationEntryPoint = notificationsAuthenticationEntryPoint;
            this.notificationsAccessDeniedHandler = notificationsAccessDeniedHandler;
        }

        @Override
        public void configure(WebSecurity web) {
            web.ignoring().antMatchers(
                "/swagger-ui.html",
                "/webjars/springfox-swagger-ui/**",
                "/swagger-resources/**",
                "/v2/**",
                "/refdata/**",
                "/health",
                "/health/liveness",
                "/health/readiness",
                "/info",
                "/favicon.ico",
                "/mock-api/**"
            );
        }

        @Override
        @SuppressWarnings(value = "SPRING_CSRF_PROTECTION_DISABLED",
            justification = "It's safe to disable CSRF protection as application is not being hit directly from the browser")
        protected void configure(HttpSecurity http) {
            try {
                http
                    .addFilterAfter(serviceAndUserAuthFilter, BearerTokenAuthenticationFilter.class)
                    .addFilterBefore(serviceAuthFilter, BearerTokenAuthenticationFilter.class)
                    .sessionManagement().sessionCreationPolicy(STATELESS).and()
                    .csrf().disable()
                    .formLogin().disable()
                    .logout().disable()
                    .authorizeRequests()
                    .antMatchers(HttpMethod.POST, "/notifications/**").permitAll()
                    .antMatchers(HttpMethod.GET, "/notifications/**").permitAll()
                    .antMatchers("/error").permitAll()
                    .anyRequest().authenticated()
                    .and()
                    .oauth2ResourceServer()
                    .jwt()
                    .jwtAuthenticationConverter(jwtAuthenticationConverter)
                    .and()
                    .and()
                    .oauth2Client()
                    .and()
                    .exceptionHandling().accessDeniedHandler(notificationsAccessDeniedHandler)
                    .authenticationEntryPoint(notificationsAuthenticationEntryPoint)
                ;

            } catch (Exception e) {
                LOG.info("Error in InternalApiSecurityConfigurationAdapter: {}", e);
            }
        }

        @Bean
        @SuppressWarnings("unchecked")
        JwtDecoder jwtDecoder() {
            NimbusJwtDecoder jwtDecoder = (NimbusJwtDecoder)
                JwtDecoders.fromOidcIssuerLocation(issuerUri);

            OAuth2TokenValidator<Jwt> audienceValidator = new AudienceValidator(Arrays.asList(allowedAudiences));

            OAuth2TokenValidator<Jwt> withTimestamp = new JwtTimestampValidator();

            // Commented issuer validation as confirmed by IDAM
            /* OAuth2TokenValidator<Jwt> withIssuer = new JwtIssuerValidator(issuerOverride);*/
            OAuth2TokenValidator<Jwt> withAudience = new DelegatingOAuth2TokenValidator<>(
                withTimestamp,
                audienceValidator
            );
            jwtDecoder.setJwtValidator(withAudience);

            return jwtDecoder;
        }
    }

}
