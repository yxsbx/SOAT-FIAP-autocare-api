package br.com.autocarehub.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.autocarehub.domain.exception.DomainException;
import br.com.autocarehub.domain.valueobject.Plate;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class VehicleTest {

    private static Vehicle vehicle() {
        return new Vehicle(UUID.randomUUID(), new Plate("ABC1D23"), "Honda", "Civic", 2020, 30000);
    }

    @Test
    void shouldValidateOldBrazilianPlate() {
        Vehicle vehicle = new Vehicle(UUID.randomUUID(), new Plate("abc-1234"), "Honda", "Civic", 2020, 30000);

        assertThat(vehicle.plate().value()).isEqualTo("ABC1234");
    }

    @Test
    void shouldValidateMercosurPlate() {
        Vehicle vehicle = new Vehicle(UUID.randomUUID(), new Plate("abc-1d23"), "Honda", "Civic", 2020, 30000);

        assertThat(vehicle.plate().value()).isEqualTo("ABC1D23");
    }

    @Test
    void shouldRejectInvalidPlate() {
        assertThatThrownBy(() -> new Vehicle(UUID.randomUUID(), new Plate("ABC12D3"), "Honda", "Civic", 2020, 30000))
                .isInstanceOf(DomainException.class)
                .hasMessage("Invalid plate");
    }

    @Test
    void shouldUpdateVehicle() {
        Vehicle vehicle = vehicle();

        vehicle.update(new Plate("DEF2G34"), "Toyota", "Corolla", 2021, 35000);

        assertThat(vehicle.plate().value()).isEqualTo("DEF2G34");
        assertThat(vehicle.brand()).isEqualTo("Toyota");
        assertThat(vehicle.model()).isEqualTo("Corolla");
        assertThat(vehicle.year()).isEqualTo(2021);
        assertThat(vehicle.mileage()).isEqualTo(35000);
    }

    @Test
    void shouldUpdateMileageOnlyWhenItDõesNotDecrease() {
        Vehicle vehicle = vehicle();

        vehicle.updateMileage(35000);

        assertThat(vehicle.mileage()).isEqualTo(35000);
        assertThatThrownBy(() -> vehicle.updateMileage(10000))
                .isInstanceOf(DomainException.class)
                .hasMessage("Mileage cannot decrease");
    }

    @Test
    void shouldActivateAndDeactivate() {
        Vehicle vehicle = vehicle();

        vehicle.deactivate();
        assertThat(vehicle.active()).isFalse();

        vehicle.activate();
        assertThat(vehicle.active()).isTrue();
    }

    @Test
    void shouldRejectInvalidVehicleData() {
        assertThatThrownBy(() -> new Vehicle(UUID.randomUUID(), new Plate("ABC1D23"), " ", "Civic", 2020, 30000))
                .isInstanceOf(DomainException.class)
                .hasMessage("Brand is required");
        assertThatThrownBy(() -> new Vehicle(UUID.randomUUID(), new Plate("ABC1D23"), "Honda", " ", 2020, 30000))
                .isInstanceOf(DomainException.class)
                .hasMessage("Model is required");
        assertThatThrownBy(() -> new Vehicle(UUID.randomUUID(), new Plate("ABC1D23"), "Honda", "Civic", 1899, 30000))
                .isInstanceOf(DomainException.class)
                .hasMessage("Invalid year");
        assertThatThrownBy(() -> new Vehicle(UUID.randomUUID(), new Plate("ABC1D23"), "Honda", "Civic", 2200, 30000))
                .isInstanceOf(DomainException.class)
                .hasMessage("Invalid year");
        assertThatThrownBy(() -> new Vehicle(UUID.randomUUID(), new Plate("ABC1D23"), "Honda", "Civic", 2020, -1))
                .isInstanceOf(DomainException.class)
                .hasMessage("Mileage cannot be negative");
        assertThatThrownBy(() -> new Plate(null))
                .isInstanceOf(DomainException.class)
                .hasMessage("Plate is required");
    }
}
