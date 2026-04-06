package com.ewalletgateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

/**
 * Endpoint không yêu cầu JWT (khớp với {@link SecurityConfig}).
 */
@Configuration
public class GatewayPublicEndpoints {

    @Bean
    public RequestMatcher gatewayPublicEndpointsMatcher() {
        return new OrRequestMatcher(
                PathPatternRequestMatcher.pathPattern("/actuator/health"),
                PathPatternRequestMatcher.pathPattern("/actuator/info"),
                PathPatternRequestMatcher.pathPattern("/swagger-ui/**"),
                PathPatternRequestMatcher.pathPattern("/swagger-ui.html"),
                PathPatternRequestMatcher.pathPattern("/v3/api-docs"),
                PathPatternRequestMatcher.pathPattern("/v3/api-docs/**"),
                PathPatternRequestMatcher.pathPattern("/api/v1/auth/register"),
                PathPatternRequestMatcher.pathPattern("/api/v1/auth/login"),
                PathPatternRequestMatcher.pathPattern("/api/v1/auth/refresh"),
                PathPatternRequestMatcher.pathPattern(HttpMethod.OPTIONS, "/**")
        );
    }
}
