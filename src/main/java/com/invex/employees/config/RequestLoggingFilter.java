package com.invex.employees.config;

import java.io.IOException;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.invex.employees.constants.ApiConstants;

import lombok.extern.slf4j.Slf4j;

/**
 * Logs every request with its headers, status and duration, and tags it with a
 * request id
 * (taken from X-Request-Id or generated) that is added to the MDC and echoed
 * back to the client.
 * Runs before Spring Security so even rejected requests (401) are traceable.
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Set<String> SENSITIVE_HEADERS = Set.of("authorization", "proxy-authorization", "cookie",
            "set-cookie", "x-api-key");
    private static final String MASK = "****";
    // Only trust client ids that look safe, so nobody can inject line breaks or
    // garbage into our logs.
    private static final Pattern SAFE_REQUEST_ID = Pattern.compile("^[A-Za-z0-9._-]{1,64}$");

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
            @NonNull FilterChain chain) throws ServletException, IOException {
        String requestId = resolveRequestId(request);
        MDC.put(ApiConstants.REQUEST_ID_MDC_KEY, requestId);
        response.setHeader(ApiConstants.REQUEST_ID_HEADER, requestId);
        long start = System.currentTimeMillis();
        try {
            log.info("--> {} {} headers={}", request.getMethod(), request.getRequestURI(), maskedHeaders(request));
            chain.doFilter(request, response);
        } finally {
            log.info("<-- {} {} status={} took={}ms", request.getMethod(), request.getRequestURI(),
                    response.getStatus(), System.currentTimeMillis() - start);
            MDC.remove(ApiConstants.REQUEST_ID_MDC_KEY);
        }
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        // Swagger UI loads dozens of static files; logging them only adds noise.
        String uri = request.getRequestURI();
        return uri.startsWith("/swagger-ui") || uri.startsWith("/v3/api-docs/swagger-config");
    }

    private static String resolveRequestId(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(ApiConstants.REQUEST_ID_HEADER))
                .filter(id -> SAFE_REQUEST_ID.matcher(id).matches())
                .orElseGet(() -> UUID.randomUUID().toString());
    }

    static Map<String, String> maskedHeaders(HttpServletRequest request) {
        return Collections.list(request.getHeaderNames()).stream()
                .collect(Collectors.toMap(Function.identity(),
                        name -> SENSITIVE_HEADERS.contains(name.toLowerCase(Locale.ROOT)) ? MASK
                                : request.getHeader(name),
                        (first, second) -> first,
                        TreeMap::new));
    }
}
