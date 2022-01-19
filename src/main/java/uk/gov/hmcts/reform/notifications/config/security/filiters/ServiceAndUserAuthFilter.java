package uk.gov.hmcts.reform.notifications.config.security.filiters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.reform.notifications.config.security.exception.UnauthorizedException;
import uk.gov.hmcts.reform.notifications.config.security.utils.SecurityUtils;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Function;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.springframework.http.HttpStatus.FORBIDDEN;

public class ServiceAndUserAuthFilter extends OncePerRequestFilter {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceAndUserAuthFilter.class);

    private final Function<HttpServletRequest, Optional<String>> userIdExtractor;
    private final SecurityUtils securityUtils;

    public ServiceAndUserAuthFilter(Function<HttpServletRequest, Optional<String>> userIdExtractor,
                                    SecurityUtils securityUtils) {
        super();
        this.userIdExtractor = userIdExtractor;
        this.securityUtils = securityUtils;
    }

    @Override
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {

        Optional<String> userIdOptional = userIdExtractor.apply(request);

        if (securityUtils.isAuthenticated()  || userIdOptional.isPresent()) {
            try {
                verifyRoleAndUserId(userIdOptional);
                LOG.info("User authentication is successful");
            } catch (UnauthorizedException ex) {
                LOG.warn("Unauthorised roles or userId in the request path", ex);
                response.sendError(FORBIDDEN.value(), " Access Denied " + ex.getMessage());
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private void verifyRoleAndUserId(Optional<String> userIdOptional) {
        UserInfo userInfo = securityUtils.getUserInfo();

        if (userIdOptional.isPresent() && !userIdOptional.get().equalsIgnoreCase(userInfo.getUid())) {
            throw new UnauthorizedException("Unauthorised userId in the path");
        }
    }

}
