package br.com.autocarehub.infrastructure.security;

import br.com.autocarehub.domain.model.User;
import java.io.Serial;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class AuthenticatedUser implements UserDetails {

    @Serial
    private static final long serialVersionUID = 1L;

    private final User user;

    public AuthenticatedUser(User user) {
        this.user = user;
    }

    public UUID id() {
        return user.id();
    }

    public @Nullable UUID customerId() {
        return user.customerId();
    }

    public String role() {
        return user.role().name();
    }

    @Override
    public @NonNull Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.role().name()));
    }

    @Override
    public String getPassword() {
        return user.passwordHash();
    }

    @Override
    public @NonNull String getUsername() {
        return user.username();
    }

    @Override
    public boolean isAccountNonExpired() {
        return user.active();
    }

    @Override
    public boolean isAccountNonLocked() {
        return user.active();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return user.active();
    }

    @Override
    public boolean isEnabled() {
        return user.active();
    }
}
