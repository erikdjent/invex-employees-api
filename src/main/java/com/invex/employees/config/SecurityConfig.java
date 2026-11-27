package com.invex.employees.config;

import java.time.OffsetDateTime;

import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.invex.employees.constants.ApiConstants;
import com.invex.employees.exception.ErrorResponse;

/**
 * Stateless HTTP Basic security. Credentials come from environment variables
 * (API_USERNAME / API_PASSWORD) through the standard spring.security.user.*
 * properties.
 * Only the health check and the API docs are public.
 */
@Configuration
public class SecurityConfig {

        private static final String[] PUBLIC_PATHS = {
                        "/actuator/health/**", "/actuator/info", "/swagger-ui.html", "/swagger-ui/**",
                        "/v3/api-docs/**", "/error"
        };
        private static final String UNAUTHORIZED_DETAIL = "Authentication is required to access this resource";

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity, AuthenticationEntryPoint entryPoint)
                        throws Exception {
                httpSecurity
                                // No browser sessions or cookies are used, so CSRF protection does not apply
                                // here.
                                .csrf(AbstractHttpConfigurer::disable)
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .authorizeHttpRequests(auth -> auth
                                                .antMatchers(PUBLIC_PATHS).permitAll()
                                                .anyRequest().authenticated())
                                .httpBasic(basic -> basic.authenticationEntryPoint(entryPoint))
                                .exceptionHandling(ex -> ex.authenticationEntryPoint(entryPoint))
                                // The H2 console renders inside frames from this same origin.
                                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));
                return httpSecurity.build();
        }

        /**
         * Returns 401 with the same error body the rest of the API uses, instead of an
         * empty response.
         */
        @Bean
        public AuthenticationEntryPoint jsonAuthenticationEntryPoint(ObjectMapper objectMapper) {
                return (request, response, authException) -> {
                        ErrorResponse body = ErrorResponse.builder()
                                        .title(HttpStatus.UNAUTHORIZED.getReasonPhrase())
                                        .status(HttpStatus.UNAUTHORIZED.value())
                                        .detail(UNAUTHORIZED_DETAIL)
                                        .instance(request.getRequestURI())
                                        .requestId(MDC.get(ApiConstants.REQUEST_ID_MDC_KEY))
                                        .timestamp(OffsetDateTime.now())
                                        .build();
                        response.setStatus(HttpStatus.UNAUTHORIZED.value());
                        response.setHeader("WWW-Authenticate", "Basic realm=\"employees-api\"");
                        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
                        objectMapper.writeValue(response.getOutputStream(), body);
                };
        }
}
