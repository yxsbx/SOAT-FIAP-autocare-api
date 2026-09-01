package br.com.autocarehub.application.usecase.demo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.autocarehub.application.port.out.DemoLeadRepository;
import br.com.autocarehub.domain.exception.DomainException;
import br.com.autocarehub.domain.model.DemoLead;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class RegisterDemoLeadUseCaseTest {

    private final InMemoryDemoLeadRepository repository = new InMemoryDemoLeadRepository();
    private final RegisterDemoLeadUseCase useCase = new RegisterDemoLeadUseCase(repository);

    @Test
    void shouldNormalizeAndSaveValidCnpj() {
        DemoLead demoLead = useCase.execute(new RegisterDemoLeadUseCase.Command(
                "Ana",
                "Oficina Central",
                "workshop",
                "ANA@EXAMPLE.COM",
                "11999999999",
                "11.222.333/0001-81",
                "São Paulo",
                "Quero uma demo"));

        assertThat(demoLead.cnpj()).isEqualTo("11222333000181");
        assertThat(demoLead.email()).isEqualTo("ana@example.com");
    }

    @Test
    void shouldRejectInvalidCnpj() {
        assertThatThrownBy(() -> useCase.execute(new RegisterDemoLeadUseCase.Command(
                        "Ana",
                        "Oficina Central",
                        "workshop",
                        "ana@example.com",
                        "11999999999",
                        "11.222.333/0001-82",
                        "São Paulo",
                        "Quero uma demo")))
                .isInstanceOf(DomainException.class)
                .hasMessage("Invalid document");
    }

    @Test
    void shouldRejectCpfInCnpjField() {
        assertThatThrownBy(() -> useCase.execute(new RegisterDemoLeadUseCase.Command(
                        "Ana",
                        "Oficina Central",
                        "workshop",
                        "ana@example.com",
                        "11999999999",
                        "529.982.247-25",
                        "São Paulo",
                        "Quero uma demo")))
                .isInstanceOf(DomainException.class)
                .hasMessage("Demo lead document must be CNPJ");
    }

    private static final class InMemoryDemoLeadRepository implements DemoLeadRepository {

        private final List<DemoLead> demoLeads = new ArrayList<>();

        @Override
        public DemoLead save(DemoLead demoLead) {
            demoLeads.add(demoLead);
            return demoLead;
        }

        @Override
        public List<DemoLead> findAll() {
            return List.copyOf(demoLeads);
        }
    }
}
