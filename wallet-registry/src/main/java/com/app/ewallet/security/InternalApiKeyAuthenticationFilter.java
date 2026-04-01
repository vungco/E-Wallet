package com.app.ewallet.security;

import com.app.ewallet.config.properties.SecurityProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class InternalApiKeyAuthenticationFilter extends OncePerRequestFilter {

    public static final String HEADER_INTERNAL_API_KEY = "X-Internal-Api-Key";

    private final SecurityProperties securityProperties;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String context = request.getContextPath();
        String path = request.getRequestURI();
        String internalPrefix = context + "/internal";

        if (!path.startsWith(internalPrefix)) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!securityProperties.requireInternalKey()) {
            filterChain.doFilter(request, response);
            return;
        }

        String expected = securityProperties.internalApiKey();
        if (!StringUtils.hasText(expected)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "INTERNAL_API_KEY is not configured");
            return;
        }

        String provided = request.getHeader(HEADER_INTERNAL_API_KEY);
        if (!expected.equals(provided)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or missing X-Internal-Api-Key");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
