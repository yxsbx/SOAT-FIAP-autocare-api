package br.com.autocarehub.infrastructure.security;

import br.com.autocarehub.interfaces.rest.generated.model.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.StaticHeadersWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private static final long HSTS_MAX_AGE_SECONDS = 31_536_000L;
    private static final long CORS_MAX_AGE_SECONDS = 3_600L;

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ObjectMapper objectMapper;
    private final String allowedOrigins;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            ObjectMapper objectMapper,
            @Value("${app.cors.allowed-origins:http://localhost:5173,http://127.0.0.1:5173}") String allowedOrigins) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.objectMapper = objectMapper;
        this.allowedOrigins = allowedOrigins;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) {
        return http.cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .headers(headers -> headers.contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'; "
                                + "script-src 'self'; "
                                + "style-src 'self' 'unsafe-inline'; "
                                + "img-src 'self' data:; "
                                + "connect-src 'self'; "
                                + "frame-ancestors 'self'"))
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
                        .referrerPolicy(referrer -> referrer.policy(
                                org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter
                                        .ReferrerPolicy.NO_REFERRER))
                        .addHeaderWriter(new StaticHeadersWriter("Cross-Origin-Resource-Policy", "same-origin"))
                        .permissionsPolicyHeader(permissions ->
                                permissions.policy("geolocation=(), microphone=(), camera=(), payment=(), usb=()"))
                        .httpStrictTransportSecurity(
                                hsts -> hsts.includeSubDomains(true).maxAgeInSeconds(HSTS_MAX_AGE_SECONDS)))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, exception) -> writeError(
                                request, response, HttpStatus.UNAUTHORIZED, "Missing or invalid credentials"))
                        .accessDeniedHandler((request, response, exception) ->
                                writeError(request, response, HttpStatus.FORBIDDEN, "Access denied")))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                "/api/v1/auth/login",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/swagger-ui-init.js",
                                "/swagger-ui-static.html",
                                "/webjars/**",
                                "/openapi.yaml",
                                "/actuator/health",
                                "/actuator/health/**")
                        .permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/v1/demo-leads")
                        .permitAll()
                        .requestMatchers(
                                org.springframework.http.HttpMethod.GET,
                                "/api/v1/users/me",
                                "/api/v1/users/me/preferences/home",
                                "/api/v1/customers/*/service-orders",
                                "/api/v1/customers/*/vehicles",
                                "/api/v1/users/partners",
                                "/api/v1/parts",
                                "/api/v1/parts/*")
                        .authenticated()
                        .requestMatchers(
                                org.springframework.http.HttpMethod.PUT,
                                "/api/v1/users/me",
                                "/api/v1/users/me/preferences/home")
                        .authenticated()
                        .requestMatchers(org.springframework.http.HttpMethod.PATCH, "/api/v1/users/me/password")
                        .authenticated()
                        .requestMatchers(
                                org.springframework.http.HttpMethod.GET, "/api/v1/customers", "/api/v1/customers/*")
                        .hasAnyRole("ADMIN", "EMPLOYEE")
                        .requestMatchers(
                                org.springframework.http.HttpMethod.GET,
                                "/api/v1/vehicles",
                                "/api/v1/vehicles/*",
                                "/api/v1/workshop-services",
                                "/api/v1/workshop-services/*",
                                "/api/v1/service-orders",
                                "/api/v1/service-orders/metrics/average-execution-time")
                        .hasAnyRole("ADMIN", "EMPLOYEE")
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/v1/demo-leads")
                        .hasRole("ADMIN")
                        .requestMatchers(
                                org.springframework.http.HttpMethod.POST, "/api/v1/workshop-services", "/api/v1/parts")
                        .hasRole("ADMIN")
                        .requestMatchers(
                                org.springframework.http.HttpMethod.POST, "/api/v1/customers", "/api/v1/vehicles")
                        .hasAnyRole("ADMIN", "EMPLOYEE")
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/v1/service-orders")
                        .hasAnyRole("ADMIN", "EMPLOYEE")
                        .requestMatchers(
                                org.springframework.http.HttpMethod.POST,
                                "/api/v1/service-orders/*/budget/decision",
                                "/api/v1/service-orders/*/budget/external-approval",
                                "/api/v1/service-orders/*/budget/external-rejection",
                                "/api/v1/service-orders/*/status/external")
                        .permitAll()
                        .requestMatchers(
                                org.springframework.http.HttpMethod.PUT,
                                "/api/v1/customers/*",
                                "/api/v1/vehicles/*",
                                "/api/v1/workshop-services/*",
                                "/api/v1/parts/*")
                        .hasRole("ADMIN")
                        .requestMatchers(
                                org.springframework.http.HttpMethod.PATCH,
                                "/api/v1/parts/*/stock",
                                "/api/v1/parts/*/stock-movement",
                                "/api/v1/parts/*/reservation",
                                "/api/v1/parts/*/reserve",
                                "/api/v1/parts/*/release-reservation",
                                "/api/v1/parts/*/commit-reservation",
                                "/api/v1/service-orders/*/status")
                        .hasAnyRole("ADMIN", "EMPLOYEE")
                        .requestMatchers(
                                org.springframework.http.HttpMethod.DELETE,
                                "/api/v1/customers/*",
                                "/api/v1/vehicles/*",
                                "/api/v1/workshop-services/*",
                                "/api/v1/parts/*")
                        .hasRole("ADMIN")
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/v1/service-orders/*")
                        .authenticated()
                        .requestMatchers(
                                org.springframework.http.HttpMethod.POST, "/api/v1/service-orders/*/budget/approve")
                        .authenticated()
                        .requestMatchers("/api/v1/**")
                        .hasAnyRole("ADMIN", "EMPLOYEE")
                        .anyRequest()
                        .authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        Set<String> origins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isBlank())
                .peek(this::rejectUnsafeCorsOrigin)
                .collect(java.util.stream.Collectors.toCollection(java.util.LinkedHashSet::new));
        if (origins.isEmpty()) {
            throw new IllegalStateException("At least one CORS origin must be configured");
        }
        origins.forEach(configuration::addAllowedOrigin);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "X-External-Service-Token"));
        configuration.setAllowCredentials(false);
        configuration.setMaxAge(CORS_MAX_AGE_SECONDS);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    private void rejectUnsafeCorsOrigin(String origin) {
        if ("*".equals(origin) || "null".equalsIgnoreCase(origin)) {
            throw new IllegalStateException("Wildcard or null CORS origins are not allowed");
        }
    }

    private void writeError(HttpServletRequest request, HttpServletResponse response, HttpStatus status, String message)
            throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(
                response.getOutputStream(),
                new ErrorResponse(
                        OffsetDateTime.now(),
                        status.value(),
                        status.getReasonPhrase(),
                        message,
                        request.getRequestURI()));
    }
}
