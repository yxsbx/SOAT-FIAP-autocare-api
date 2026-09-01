package br.com.autocarehub.interfaces.rest.controller;

import br.com.autocarehub.application.usecase.demo.ListDemoLeadsUseCase;
import br.com.autocarehub.application.usecase.demo.RegisterDemoLeadUseCase;
import br.com.autocarehub.domain.model.DemoLead;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/demo-leads")
public class DemoLeadsController {

    private final RegisterDemoLeadUseCase registerDemoLeadUseCase;
    private final ListDemoLeadsUseCase listDemoLeadsUseCase;

    public DemoLeadsController(
            RegisterDemoLeadUseCase registerDemoLeadUseCase, ListDemoLeadsUseCase listDemoLeadsUseCase) {
        this.registerDemoLeadUseCase = registerDemoLeadUseCase;
        this.listDemoLeadsUseCase = listDemoLeadsUseCase;
    }

    @PostMapping
    public ResponseEntity<DemoLeadResponse> register(@Valid @RequestBody DemoLeadRequest request) {
        DemoLead demoLead = registerDemoLeadUseCase.execute(request.toCommand());
        return ResponseEntity.status(HttpStatus.CREATED).body(DemoLeadResponse.from(demoLead));
    }

    @GetMapping
    public ResponseEntity<List<DemoLeadResponse>> list() {
        return ResponseEntity.ok(listDemoLeadsUseCase.execute().stream()
                .map(DemoLeadResponse::from)
                .toList());
    }

    public record DemoLeadRequest(
            @NotBlank @Size(max = 120) String contactName,
            @NotBlank @Size(max = 120) String companyName,
            @NotBlank @Pattern(regexp = "^(workshop|partsStore)$") String demoProfile,
            @NotBlank @Email @Size(max = 160) String email,
            @NotBlank @Size(min = 8, max = 30) String phone,
            @NotBlank @Size(min = 6, max = 40) @Pattern(regexp = "^[A-Za-z0-9./-]+$") String cnpj,
            @Size(max = 120) String city,
            @Size(max = 500) String message) {

        RegisterDemoLeadUseCase.Command toCommand() {
            return new RegisterDemoLeadUseCase.Command(
                    contactName, companyName, demoProfile, email, phone, cnpj, city, message);
        }
    }

    public record DemoLeadResponse(
            UUID id,
            String contactName,
            String companyName,
            String demoProfile,
            String email,
            String phone,
            String cnpj,
            String city,
            String message,
            LocalDateTime createdAt) {

        static DemoLeadResponse from(DemoLead demoLead) {
            return new DemoLeadResponse(
                    demoLead.id(),
                    demoLead.contactName(),
                    demoLead.companyName(),
                    demoLead.demoProfile(),
                    demoLead.email(),
                    demoLead.phone(),
                    demoLead.cnpj(),
                    demoLead.city(),
                    demoLead.message(),
                    demoLead.createdAt());
        }
    }
}
