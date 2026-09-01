package br.com.autocarehub.infrastructure.persistence.repository;

import br.com.autocarehub.application.port.out.UserRepository;
import br.com.autocarehub.domain.model.User;
import br.com.autocarehub.infrastructure.persistence.mapper.UserJpaMapper;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepositoryAdapter implements UserRepository {

    private final UserJpaRepository userJpaRepository;

    public UserRepositoryAdapter(UserJpaRepository userJpaRepository) {
        this.userJpaRepository = userJpaRepository;
    }

    @Override
    public User save(User user) {
        return UserJpaMapper.toDomain(userJpaRepository.save(UserJpaMapper.toEntity(user)));
    }

    @Override
    public Optional<User> findById(UUID id) {
        return userJpaRepository.findById(id).map(UserJpaMapper::toDomain);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userJpaRepository.findByUsername(username).map(UserJpaMapper::toDomain);
    }

    @Override
    public List<User> findAll() {
        return userJpaRepository.findAll().stream().map(UserJpaMapper::toDomain).toList();
    }
}
