package br.com.autocarehub.infrastructure.config;

import br.com.autocarehub.application.port.out.AuthenticationGateway;
import br.com.autocarehub.application.port.out.CompanyRepository;
import br.com.autocarehub.application.port.out.CustomerRepository;
import br.com.autocarehub.application.port.out.DemoLeadRepository;
import br.com.autocarehub.application.port.out.PartRepository;
import br.com.autocarehub.application.port.out.PasswordHasher;
import br.com.autocarehub.application.port.out.ServiceOrderRepository;
import br.com.autocarehub.application.port.out.StockMovementRepository;
import br.com.autocarehub.application.port.out.UserPreferenceRepository;
import br.com.autocarehub.application.port.out.UserRepository;
import br.com.autocarehub.application.port.out.VehicleRepository;
import br.com.autocarehub.application.port.out.WorkshopServiceRepository;
import br.com.autocarehub.application.usecase.auth.LoginUseCase;
import br.com.autocarehub.application.usecase.customer.CreateCustomerUseCase;
import br.com.autocarehub.application.usecase.customer.DeleteCustomerUseCase;
import br.com.autocarehub.application.usecase.customer.FindCustomerUseCase;
import br.com.autocarehub.application.usecase.customer.ListCustomersUseCase;
import br.com.autocarehub.application.usecase.customer.UpdateCustomerUseCase;
import br.com.autocarehub.application.usecase.demo.ListDemoLeadsUseCase;
import br.com.autocarehub.application.usecase.demo.RegisterDemoLeadUseCase;
import br.com.autocarehub.application.usecase.part.CommitPartReservationUseCase;
import br.com.autocarehub.application.usecase.part.ConfigurePartReservationUseCase;
import br.com.autocarehub.application.usecase.part.CreatePartUseCase;
import br.com.autocarehub.application.usecase.part.DeletePartUseCase;
import br.com.autocarehub.application.usecase.part.FindPartUseCase;
import br.com.autocarehub.application.usecase.part.ListPartsUseCase;
import br.com.autocarehub.application.usecase.part.RegisterPartStockMovementUseCase;
import br.com.autocarehub.application.usecase.part.ReleasePartReservationUseCase;
import br.com.autocarehub.application.usecase.part.ReservePartStockUseCase;
import br.com.autocarehub.application.usecase.part.UpdatePartStockUseCase;
import br.com.autocarehub.application.usecase.part.UpdatePartUseCase;
import br.com.autocarehub.application.usecase.serviceorder.AddPartToServiceOrderUseCase;
import br.com.autocarehub.application.usecase.serviceorder.AddServiceToServiceOrderUseCase;
import br.com.autocarehub.application.usecase.serviceorder.ApproveServiceOrderBudgetUseCase;
import br.com.autocarehub.application.usecase.serviceorder.CreateServiceOrderUseCase;
import br.com.autocarehub.application.usecase.serviceorder.DecideServiceOrderBudgetUseCase;
import br.com.autocarehub.application.usecase.serviceorder.FindServiceOrderUseCase;
import br.com.autocarehub.application.usecase.serviceorder.GenerateServiceOrderBudgetUseCase;
import br.com.autocarehub.application.usecase.serviceorder.GetAverageServiceOrderExecutionTimeUseCase;
import br.com.autocarehub.application.usecase.serviceorder.ListServiceOrdersByCustomerUseCase;
import br.com.autocarehub.application.usecase.serviceorder.ListServiceOrdersUseCase;
import br.com.autocarehub.application.usecase.serviceorder.TrackServiceOrderUseCase;
import br.com.autocarehub.application.usecase.serviceorder.UpdateServiceOrderStatusUseCase;
import br.com.autocarehub.application.usecase.user.ChangeUserPasswordUseCase;
import br.com.autocarehub.application.usecase.user.CreateManagedUserUseCase;
import br.com.autocarehub.application.usecase.user.CreateUserUseCase;
import br.com.autocarehub.application.usecase.user.GetUserPreferenceUseCase;
import br.com.autocarehub.application.usecase.user.GetUserUseCase;
import br.com.autocarehub.application.usecase.user.ListManageableCompaniesUseCase;
import br.com.autocarehub.application.usecase.user.ListManageableUsersUseCase;
import br.com.autocarehub.application.usecase.user.ListPartnerUsersUseCase;
import br.com.autocarehub.application.usecase.user.ListUsersUseCase;
import br.com.autocarehub.application.usecase.user.SaveUserPreferenceUseCase;
import br.com.autocarehub.application.usecase.user.UpdateManagedUserUseCase;
import br.com.autocarehub.application.usecase.user.UpdateUserUseCase;
import br.com.autocarehub.application.usecase.vehicle.CreateVehicleUseCase;
import br.com.autocarehub.application.usecase.vehicle.DeleteVehicleUseCase;
import br.com.autocarehub.application.usecase.vehicle.FindVehicleUseCase;
import br.com.autocarehub.application.usecase.vehicle.ListVehiclesByCustomerUseCase;
import br.com.autocarehub.application.usecase.vehicle.ListVehiclesUseCase;
import br.com.autocarehub.application.usecase.vehicle.UpdateVehicleUseCase;
import br.com.autocarehub.application.usecase.workshopservice.CreateWorkshopServiceUseCase;
import br.com.autocarehub.application.usecase.workshopservice.DeleteWorkshopServiceUseCase;
import br.com.autocarehub.application.usecase.workshopservice.FindWorkshopServiceUseCase;
import br.com.autocarehub.application.usecase.workshopservice.ListWorkshopServicesUseCase;
import br.com.autocarehub.application.usecase.workshopservice.UpdateWorkshopServiceUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationUseCaseConfig {

    @Bean
    LoginUseCase loginUseCase(AuthenticationGateway authenticationGateway) {
        return new LoginUseCase(authenticationGateway);
    }

    @Bean
    RegisterDemoLeadUseCase registerDemoLeadUseCase(DemoLeadRepository repository) {
        return new RegisterDemoLeadUseCase(repository);
    }

    @Bean
    ListDemoLeadsUseCase listDemoLeadsUseCase(DemoLeadRepository repository) {
        return new ListDemoLeadsUseCase(repository);
    }

    @Bean
    GetUserUseCase getUserUseCase(UserRepository repository) {
        return new GetUserUseCase(repository);
    }

    @Bean
    ListUsersUseCase listUsersUseCase(UserRepository repository) {
        return new ListUsersUseCase(repository);
    }

    @Bean
    ListManageableUsersUseCase listManageableUsersUseCase(
            UserRepository userRepository, CompanyRepository companyRepository, ListUsersUseCase listUsersUseCase) {
        return new ListManageableUsersUseCase(userRepository, companyRepository, listUsersUseCase);
    }

    @Bean
    ListPartnerUsersUseCase listPartnerUsersUseCase(ListUsersUseCase listUsersUseCase) {
        return new ListPartnerUsersUseCase(listUsersUseCase);
    }

    @Bean
    ListManageableCompaniesUseCase listManageableCompaniesUseCase(
            UserRepository userRepository, CompanyRepository companyRepository) {
        return new ListManageableCompaniesUseCase(userRepository, companyRepository);
    }

    @Bean
    CreateUserUseCase createUserUseCase(UserRepository repository, PasswordHasher passwordHasher) {
        return new CreateUserUseCase(repository, passwordHasher);
    }

    @Bean
    CreateManagedUserUseCase createManagedUserUseCase(
            UserRepository userRepository, CompanyRepository companyRepository, CreateUserUseCase createUserUseCase) {
        return new CreateManagedUserUseCase(userRepository, companyRepository, createUserUseCase);
    }

    @Bean
    UpdateUserUseCase updateUserUseCase(UserRepository repository) {
        return new UpdateUserUseCase(repository);
    }

    @Bean
    UpdateManagedUserUseCase updateManagedUserUseCase(
            UserRepository userRepository, CompanyRepository companyRepository, UpdateUserUseCase updateUserUseCase) {
        return new UpdateManagedUserUseCase(userRepository, companyRepository, updateUserUseCase);
    }

    @Bean
    ChangeUserPasswordUseCase changeUserPasswordUseCase(UserRepository repository, PasswordHasher passwordHasher) {
        return new ChangeUserPasswordUseCase(repository, passwordHasher);
    }

    @Bean
    GetUserPreferenceUseCase getUserPreferenceUseCase(UserPreferenceRepository repository) {
        return new GetUserPreferenceUseCase(repository);
    }

    @Bean
    SaveUserPreferenceUseCase saveUserPreferenceUseCase(UserPreferenceRepository repository) {
        return new SaveUserPreferenceUseCase(repository);
    }

    @Bean
    CreateCustomerUseCase createCustomerUseCase(
            CustomerRepository repository, UserRepository userRepository, PasswordHasher passwordHasher) {
        return new CreateCustomerUseCase(repository, userRepository, passwordHasher);
    }

    @Bean
    UpdateCustomerUseCase updateCustomerUseCase(CustomerRepository repository) {
        return new UpdateCustomerUseCase(repository);
    }

    @Bean
    FindCustomerUseCase findCustomerUseCase(CustomerRepository repository) {
        return new FindCustomerUseCase(repository);
    }

    @Bean
    ListCustomersUseCase listCustomersUseCase(CustomerRepository repository) {
        return new ListCustomersUseCase(repository);
    }

    @Bean
    DeleteCustomerUseCase deleteCustomerUseCase(CustomerRepository repository) {
        return new DeleteCustomerUseCase(repository);
    }

    @Bean
    CreateVehicleUseCase createVehicleUseCase(
            VehicleRepository vehicleRepository, CustomerRepository customerRepository) {
        return new CreateVehicleUseCase(vehicleRepository, customerRepository);
    }

    @Bean
    UpdateVehicleUseCase updateVehicleUseCase(VehicleRepository repository) {
        return new UpdateVehicleUseCase(repository);
    }

    @Bean
    FindVehicleUseCase findVehicleUseCase(VehicleRepository repository) {
        return new FindVehicleUseCase(repository);
    }

    @Bean
    ListVehiclesUseCase listVehiclesUseCase(VehicleRepository repository) {
        return new ListVehiclesUseCase(repository);
    }

    @Bean
    DeleteVehicleUseCase deleteVehicleUseCase(VehicleRepository repository) {
        return new DeleteVehicleUseCase(repository);
    }

    @Bean
    ListVehiclesByCustomerUseCase listVehiclesByCustomerUseCase(
            VehicleRepository vehicleRepository, CustomerRepository customerRepository) {
        return new ListVehiclesByCustomerUseCase(vehicleRepository, customerRepository);
    }

    @Bean
    CreateWorkshopServiceUseCase createWorkshopServiceUseCase(WorkshopServiceRepository repository) {
        return new CreateWorkshopServiceUseCase(repository);
    }

    @Bean
    UpdateWorkshopServiceUseCase updateWorkshopServiceUseCase(WorkshopServiceRepository repository) {
        return new UpdateWorkshopServiceUseCase(repository);
    }

    @Bean
    FindWorkshopServiceUseCase findWorkshopServiceUseCase(WorkshopServiceRepository repository) {
        return new FindWorkshopServiceUseCase(repository);
    }

    @Bean
    ListWorkshopServicesUseCase listWorkshopServicesUseCase(WorkshopServiceRepository repository) {
        return new ListWorkshopServicesUseCase(repository);
    }

    @Bean
    DeleteWorkshopServiceUseCase deleteWorkshopServiceUseCase(WorkshopServiceRepository repository) {
        return new DeleteWorkshopServiceUseCase(repository);
    }

    @Bean
    CreatePartUseCase createPartUseCase(PartRepository repository) {
        return new CreatePartUseCase(repository);
    }

    @Bean
    UpdatePartUseCase updatePartUseCase(PartRepository repository) {
        return new UpdatePartUseCase(repository);
    }

    @Bean
    FindPartUseCase findPartUseCase(PartRepository repository) {
        return new FindPartUseCase(repository);
    }

    @Bean
    ListPartsUseCase listPartsUseCase(PartRepository repository) {
        return new ListPartsUseCase(repository);
    }

    @Bean
    DeletePartUseCase deletePartUseCase(PartRepository repository) {
        return new DeletePartUseCase(repository);
    }

    @Bean
    UpdatePartStockUseCase updatePartStockUseCase(PartRepository repository) {
        return new UpdatePartStockUseCase(repository);
    }

    @Bean
    RegisterPartStockMovementUseCase registerPartStockMovementUseCase(
            PartRepository partRepository, StockMovementRepository stockMovementRepository) {
        return new RegisterPartStockMovementUseCase(partRepository, stockMovementRepository);
    }

    @Bean
    ConfigurePartReservationUseCase configurePartReservationUseCase(PartRepository repository) {
        return new ConfigurePartReservationUseCase(repository);
    }

    @Bean
    ReservePartStockUseCase reservePartStockUseCase(PartRepository repository) {
        return new ReservePartStockUseCase(repository);
    }

    @Bean
    ReleasePartReservationUseCase releasePartReservationUseCase(PartRepository repository) {
        return new ReleasePartReservationUseCase(repository);
    }

    @Bean
    CommitPartReservationUseCase commitPartReservationUseCase(
            PartRepository partRepository, StockMovementRepository stockMovementRepository) {
        return new CommitPartReservationUseCase(partRepository, stockMovementRepository);
    }

    @Bean
    CreateServiceOrderUseCase createServiceOrderUseCase(
            ServiceOrderRepository serviceOrderRepository,
            CustomerRepository customerRepository,
            VehicleRepository vehicleRepository,
            WorkshopServiceRepository workshopServiceRepository,
            PartRepository partRepository) {
        return new CreateServiceOrderUseCase(
                serviceOrderRepository,
                customerRepository,
                vehicleRepository,
                workshopServiceRepository,
                partRepository);
    }

    @Bean
    FindServiceOrderUseCase findServiceOrderUseCase(ServiceOrderRepository repository) {
        return new FindServiceOrderUseCase(repository);
    }

    @Bean
    ListServiceOrdersUseCase listServiceOrdersUseCase(ServiceOrderRepository repository) {
        return new ListServiceOrdersUseCase(repository);
    }

    @Bean
    AddServiceToServiceOrderUseCase addServiceToServiceOrderUseCase(
            ServiceOrderRepository serviceOrderRepository, WorkshopServiceRepository workshopServiceRepository) {
        return new AddServiceToServiceOrderUseCase(serviceOrderRepository, workshopServiceRepository);
    }

    @Bean
    AddPartToServiceOrderUseCase addPartToServiceOrderUseCase(
            ServiceOrderRepository serviceOrderRepository, PartRepository partRepository) {
        return new AddPartToServiceOrderUseCase(serviceOrderRepository, partRepository);
    }

    @Bean
    GenerateServiceOrderBudgetUseCase generateServiceOrderBudgetUseCase(
            ServiceOrderRepository repository, PartRepository partRepository) {
        return new GenerateServiceOrderBudgetUseCase(repository, partRepository);
    }

    @Bean
    ApproveServiceOrderBudgetUseCase approveServiceOrderBudgetUseCase(
            ServiceOrderRepository repository, PartRepository partRepository) {
        return new ApproveServiceOrderBudgetUseCase(repository, partRepository);
    }

    @Bean
    DecideServiceOrderBudgetUseCase decideServiceOrderBudgetUseCase(
            ServiceOrderRepository repository, PartRepository partRepository) {
        return new DecideServiceOrderBudgetUseCase(repository, partRepository);
    }

    @Bean
    UpdateServiceOrderStatusUseCase updateServiceOrderStatusUseCase(
            ServiceOrderRepository repository, PartRepository partRepository) {
        return new UpdateServiceOrderStatusUseCase(repository, partRepository);
    }

    @Bean
    ListServiceOrdersByCustomerUseCase listServiceOrdersByCustomerUseCase(
            ServiceOrderRepository serviceOrderRepository, CustomerRepository customerRepository) {
        return new ListServiceOrdersByCustomerUseCase(serviceOrderRepository, customerRepository);
    }

    @Bean
    GetAverageServiceOrderExecutionTimeUseCase getAverageServiceOrderExecutionTimeUseCase(
            ServiceOrderRepository serviceOrderRepository) {
        return new GetAverageServiceOrderExecutionTimeUseCase(serviceOrderRepository);
    }

    @Bean
    TrackServiceOrderUseCase trackServiceOrderUseCase(
            ServiceOrderRepository serviceOrderRepository,
            CustomerRepository customerRepository,
            VehicleRepository vehicleRepository) {
        return new TrackServiceOrderUseCase(serviceOrderRepository, customerRepository, vehicleRepository);
    }
}
