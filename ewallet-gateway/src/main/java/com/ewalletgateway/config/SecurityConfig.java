package com.ewalletgateway.config;

import com.ewalletgateway.config.properties.JwtProperties;
import com.ewalletgateway.security.GatewayAccessDeniedHandler;
import com.ewalletgateway.security.JwtAuthenticationEntryPoint;
import com.ewalletgateway.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;

@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(JwtProperties.class)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final GatewayAccessDeniedHandler gatewayAccessDeniedHandler;
    private final RequestMatcher gatewayPublicEndpointsMatcher;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(gatewayPublicEndpointsMatcher).permitAll()
                        .requestMatchers(
                                "/api/v1/wallets/**",
                                "/api/v1/users/**",
                                "/api/v1/transfers/**",
                                "/api/v1/transactions/**",
                                "/api/v1/notifications/**",
                                "/api/v1/auth/logout",
                                "/api/v1/auth/logout/all"
                        ).authenticated()
                        .anyRequest().denyAll()
                )
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(gatewayAccessDeniedHandler)
                )
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
