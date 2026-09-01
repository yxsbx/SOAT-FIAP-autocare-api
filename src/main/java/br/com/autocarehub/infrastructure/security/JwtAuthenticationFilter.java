package br.com.autocarehub.infrastructure.security;

import br.com.autocarehub.domain.enums.UserRole;
import br.com.autocarehub.domain.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = authorizationHeader.substring(7);
            authenticate(token, request);
        } catch (JwtException | IllegalArgumentException exception) {
            SecurityContextHolder.clearContext();
        }
        filterChain.doFilter(request, response);
    }

    private void authenticate(String token, HttpServletRequest request) {
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            return;
        }

        String username = jwtService.extractUsername(token);
        try {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            if (jwtService.isTokenValid(token, userDetails)) {
                setAuthentication(userDetails, request);
            }
        } catch (UsernameNotFoundException exception) {
            UserDetails userDetails = externalCustomerUser(jwtService.extractClaims(token));
            if (userDetails != null) {
                setAuthentication(userDetails, request);
            }
        }
    }

    private void setAuthentication(UserDetails userDetails, HttpServletRequest request) {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private UserDetails externalCustomerUser(Claims claims) {
        if (claims == null || claims.getExpiration() == null || !claims.getExpiration().after(new Date())) {
            return null;
        }
        if (!"CUSTOMER".equals(claims.get("role", String.class))) {
            return null;
        }
        String customerIdValue = claims.get("customerId", String.class);
        if (customerIdValue == null || customerIdValue.isBlank()) {
            return null;
        }
        UUID customerId = UUID.fromString(customerIdValue);
        String document = claims.get("document", String.class);
        String username = document == null || document.isBlank() ? claims.getSubject() : document;
        UUID userId = UUID.nameUUIDFromBytes(("external-customer:" + customerId).getBytes(StandardCharsets.UTF_8));
        User user = new User(
                userId,
                username,
                "external-authenticated-user",
                UserRole.CUSTOMER,
                customerId,
                null,
                username,
                "CUSTOMER",
                "",
                "",
                "",
                List.of("customer:self"),
                true,
                LocalDateTime.now());
        return new AuthenticatedUser(user);
    }
}
