package br.com.autocarehub.application.usecase.user;

import br.com.autocarehub.application.port.out.UserPreferenceRepository;
import java.util.UUID;

public class GetUserPreferenceUseCase {

    private final UserPreferenceRepository userPreferenceRepository;

    public GetUserPreferenceUseCase(UserPreferenceRepository userPreferenceRepository) {
        this.userPreferenceRepository = userPreferenceRepository;
    }

    public String execute(UUID userId, String key, String fallbackJson) {
        return userPreferenceRepository.findValue(userId, key).orElse(fallbackJson);
    }
}
