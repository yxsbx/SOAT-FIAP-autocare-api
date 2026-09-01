package br.com.autocarehub.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.autocarehub.domain.exception.DomainException;
import br.com.autocarehub.domain.valueobject.Money;
import org.junit.jupiter.api.Test;

class WorkshopServiceTest {

    private static WorkshopService service() {
        return new WorkshopService("Oil change", "Oil and filter replacement", Money.of("100.00"), 60);
    }

    @Test
    void shouldNotAcceptPriceLessThanOrEqualToZero() {
        assertThatThrownBy(() -> new WorkshopService("Oil change", "Oil and filter replacement", Money.zero(), 60))
                .isInstanceOf(DomainException.class)
                .hasMessage("Base price must be greater than zero");
    }

    @Test
    void shouldNotAcceptEstimatedTimeLessThanOrEqualToZero() {
        assertThatThrownBy(() -> new WorkshopService("Oil change", "Oil and filter replacement", Money.of("100.00"), 0))
                .isInstanceOf(DomainException.class)
                .hasMessage("Estimated time must be greater than zero");
    }

    @Test
    void shouldUpdateWorkshopService() {
        WorkshopService service = service();

        service.update("Brake repair", "Brake pad replacement", Money.of("250.00"), 120);

        assertThat(service.name()).isEqualTo("Brake repair");
        assertThat(service.description()).isEqualTo("Brake pad replacement");
        assertThat(service.basePrice().value()).isEqualByComparingTo("250.00");
        assertThat(service.estimatedTimeInMinutes()).isEqualTo(120);
    }

    @Test
    void shouldActivateAndDeactivate() {
        WorkshopService service = service();

        service.deactivate();
        assertThat(service.active()).isFalse();

        service.activate();
        assertThat(service.active()).isTrue();
    }

    @Test
    void shouldRejectBlankNameAndDescription() {
        assertThatThrownBy(() -> new WorkshopService(" ", "Oil and filter replacement", Money.of("100.00"), 60))
                .isInstanceOf(DomainException.class)
                .hasMessage("Name is required");
        assertThatThrownBy(() -> new WorkshopService("Oil change", " ", Money.of("100.00"), 60))
                .isInstanceOf(DomainException.class)
                .hasMessage("Description is required");
    }
}
