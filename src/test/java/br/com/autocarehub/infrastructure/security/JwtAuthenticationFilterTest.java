package br.com.autocarehub.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import br.com.autocarehub.domain.enums.UserRole;
import br.com.autocarehub.domain.model.User;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;

class JwtAuthenticationFilterTest {

    private final JwtService jwtService = mock(JwtService.class);
    private final UserDetailsService userDetailsService = mock(UserDetailsService.class);
    private final FilterChain filterChain = mock(FilterChain.class);
    private final JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService, userDetailsService);

    private static AuthenticatedUser authenticatedUser() {
        return new AuthenticatedUser(new User(
                UUID.randomUUID(),
                "admin@autocarehub.com",
                "$2a$10$hashhashhashhashhashhashhashhashhashhashhashhashhash",
                UserRole.ADMIN,
                null,
                UUID.fromString("90000000-0000-0000-0000-000000000011"),
                "Admin",
                "admin",
                "AutoCare",
                "Oficina",
                "Gestor",
                List.of("users:read"),
                true,
                LocalDateTime.now()));
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldContinueWithoutAuthenticationWhenAuthorizationHeaderIsMissingOrInvalid() throws Exception {
        MockHttpServletRequest noHeader = new MockHttpServletRequest();
        MockHttpServletResponse noHeaderResponse = new MockHttpServletResponse();
        MockHttpServletRequest invalidPrefix = new MockHttpServletRequest();
        MockHttpServletResponse invalidPrefixResponse = new MockHttpServletResponse();
        invalidPrefix.addHeader("Authorization", "Basic abc");

        filter.doFilter(noHeader, noHeaderResponse, filterChain);
        filter.doFilter(invalidPrefix, invalidPrefixResponse, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verifyNoInteractions(jwtService, userDetailsService);
        verify(filterChain).doFilter(noHeader, noHeaderResponse);
        verify(filterChain).doFilter(invalidPrefix, invalidPrefixResponse);
    }

    @Test
    void shouldAuthenticateValidBearerToken() throws Exception {
        AuthenticatedUser user = authenticatedUser();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader("Authorization", "Bearer valid-token");
        when(jwtService.extractUsername("valid-token")).thenReturn(user.getUsername());
        when(userDetailsService.loadUserByUsername(user.getUsername())).thenReturn(user);
        when(jwtService.isTokenValid("valid-token", user)).thenReturn(true);

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName())
                .isEqualTo(user.getUsername());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldContinueWithoutAuthenticationWhenBearerTokenIsNotValidForLoadedUser() throws Exception {
        AuthenticatedUser user = authenticatedUser();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader("Authorization", "Bearer invalid-for-user");
        when(jwtService.extractUsername("invalid-for-user")).thenReturn(user.getUsername());
        when(userDetailsService.loadUserByUsername(user.getUsername())).thenReturn(user);
        when(jwtService.isTokenValid("invalid-for-user", user)).thenReturn(false);

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldClearSecurityContextWhenTokenIsInvalid() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader("Authorization", "Bearer invalid-token");
        when(jwtService.extractUsername("invalid-token")).thenThrow(new JwtException("invalid"));

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }
}
