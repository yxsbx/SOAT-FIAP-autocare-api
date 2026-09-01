package br.com.autocarehub.infrastructure.security;

import br.com.autocarehub.infrastructure.persistence.entity.UserJpaEntity;
import br.com.autocarehub.infrastructure.persistence.mapper.UserJpaMapper;
import br.com.autocarehub.infrastructure.persistence.repository.UserJpaRepository;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@NullMarked
public class DatabaseUserDetailsService implements UserDetailsService {

    private final UserJpaRepository userJpaRepository;

    public DatabaseUserDetailsService(UserJpaRepository userJpaRepository) {
        this.userJpaRepository = userJpaRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        return userJpaRepository
                .findByUsername(username)
                .filter(UserJpaEntity::isActive)
                .map(UserJpaMapper::toDomain)
                .map(AuthenticatedUser::new)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}
