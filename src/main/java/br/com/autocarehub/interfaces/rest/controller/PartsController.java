package br.com.autocarehub.interfaces.rest.controller;

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
import br.com.autocarehub.domain.model.Part;
import br.com.autocarehub.domain.valueobject.Money;
import br.com.autocarehub.interfaces.rest.generated.api.PartsApi;
import br.com.autocarehub.interfaces.rest.generated.model.CommitPartReservationRequest;
import br.com.autocarehub.interfaces.rest.generated.model.ConfigurePartReservationRequest;
import br.com.autocarehub.interfaces.rest.generated.model.CreatePartRequest;
import br.com.autocarehub.interfaces.rest.generated.model.PartListResponse;
import br.com.autocarehub.interfaces.rest.generated.model.PartQuantityRequest;
import br.com.autocarehub.interfaces.rest.generated.model.PartResponse;
import br.com.autocarehub.interfaces.rest.generated.model.StockMovementRequest;
import br.com.autocarehub.interfaces.rest.generated.model.UpdatePartRequest;
import br.com.autocarehub.interfaces.rest.generated.model.UpdatePartStockRequest;
import br.com.autocarehub.interfaces.rest.mapper.PartRestMapper;
import java.math.BigDecimal;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PartsController implements PartsApi {

    private final CreatePartUseCase createPartUseCase;
    private final UpdatePartUseCase updatePartUseCase;
    private final FindPartUseCase findPartUseCase;
    private final ListPartsUseCase listPartsUseCase;
    private final DeletePartUseCase deletePartUseCase;
    private final UpdatePartStockUseCase updatePartStockUseCase;
    private final RegisterPartStockMovementUseCase registerPartStockMovementUseCase;
    private final ConfigurePartReservationUseCase configurePartReservationUseCase;
    private final ReservePartStockUseCase reservePartStockUseCase;
    private final ReleasePartReservationUseCase releasePartReservationUseCase;
    private final CommitPartReservationUseCase commitPartReservationUseCase;

    public PartsController(
            CreatePartUseCase createPartUseCase,
            UpdatePartUseCase updatePartUseCase,
            FindPartUseCase findPartUseCase,
            ListPartsUseCase listPartsUseCase,
            DeletePartUseCase deletePartUseCase,
            UpdatePartStockUseCase updatePartStockUseCase,
            RegisterPartStockMovementUseCase registerPartStockMovementUseCase,
            ConfigurePartReservationUseCase configurePartReservationUseCase,
            ReservePartStockUseCase reservePartStockUseCase,
            ReleasePartReservationUseCase releasePartReservationUseCase,
            CommitPartReservationUseCase commitPartReservationUseCase) {
        this.createPartUseCase = createPartUseCase;
        this.updatePartUseCase = updatePartUseCase;
        this.findPartUseCase = findPartUseCase;
        this.listPartsUseCase = listPartsUseCase;
        this.deletePartUseCase = deletePartUseCase;
        this.updatePartStockUseCase = updatePartStockUseCase;
        this.registerPartStockMovementUseCase = registerPartStockMovementUseCase;
        this.configurePartReservationUseCase = configurePartReservationUseCase;
        this.reservePartStockUseCase = reservePartStockUseCase;
        this.releasePartReservationUseCase = releasePartReservationUseCase;
        this.commitPartReservationUseCase = commitPartReservationUseCase;
    }

    @Override
    public ResponseEntity<PartResponse> createPart(CreatePartRequest createPartRequest) {
        Part part = createPartUseCase.execute(PartRestMapper.toCommand(createPartRequest));
        return ResponseEntity.status(HttpStatus.CREATED).body(PartRestMapper.toResponse(part));
    }

    @Override
    public ResponseEntity<Void> deletePart(UUID partId) {
        deletePartUseCase.execute(partId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<PartResponse> getPartById(UUID partId) {
        return ResponseEntity.ok(PartRestMapper.toResponse(findPartUseCase.execute(partId)));
    }

    @Override
    public ResponseEntity<PartListResponse> listParts(
            Integer page, Integer size, @Nullable Boolean active, @Nullable Boolean lowStock) {
        return ResponseEntity.ok(PartRestMapper.toListResponse(
                listPartsUseCase.execute(PartRestMapper.toQuery(active, lowStock)), page, size));
    }

    @Override
    public ResponseEntity<PartResponse> updatePart(UUID partId, UpdatePartRequest updatePartRequest) {
        Part part = updatePartUseCase.execute(PartRestMapper.toCommand(partId, updatePartRequest));
        return ResponseEntity.ok(PartRestMapper.toResponse(part));
    }

    @Override
    public ResponseEntity<PartResponse> updatePartStock(UUID partId, UpdatePartStockRequest updatePartStockRequest) {
        Part part = updatePartStockUseCase.execute(PartRestMapper.toCommand(partId, updatePartStockRequest));
        return ResponseEntity.ok(PartRestMapper.toResponse(part));
    }

    @Override
    public ResponseEntity<PartResponse> registerStockMovement(UUID partId, StockMovementRequest stockMovementRequest) {
        Part part = registerPartStockMovementUseCase.execute(new RegisterPartStockMovementUseCase.Command(
                partId,
                RegisterPartStockMovementUseCase.MovementType.valueOf(
                        stockMovementRequest.getType().getValue()),
                stockMovementRequest.getQuantity(),
                stockMovementRequest.getUnitCost() == null
                        ? null
                        : new Money(BigDecimal.valueOf(stockMovementRequest.getUnitCost())),
                stockMovementRequest.getUnitPrice() == null
                        ? null
                        : new Money(BigDecimal.valueOf(stockMovementRequest.getUnitPrice())),
                stockMovementRequest.getReason()));
        return ResponseEntity.ok(PartRestMapper.toResponse(part));
    }

    @Override
    public ResponseEntity<PartResponse> configurePartReservation(
            UUID partId, ConfigurePartReservationRequest configurePartReservationRequest) {
        Part part = configurePartReservationUseCase.execute(new ConfigurePartReservationUseCase.Command(
                partId, configurePartReservationRequest.getReservationDays()));
        return ResponseEntity.ok(PartRestMapper.toResponse(part));
    }

    @Override
    public ResponseEntity<PartResponse> reservePart(UUID partId, PartQuantityRequest partQuantityRequest) {
        Part part = reservePartStockUseCase.execute(
                new ReservePartStockUseCase.Command(partId, partQuantityRequest.getQuantity()));
        return ResponseEntity.ok(PartRestMapper.toResponse(part));
    }

    @Override
    public ResponseEntity<PartResponse> releaseReservation(UUID partId, PartQuantityRequest partQuantityRequest) {
        Part part = releasePartReservationUseCase.execute(
                new ReleasePartReservationUseCase.Command(partId, partQuantityRequest.getQuantity()));
        return ResponseEntity.ok(PartRestMapper.toResponse(part));
    }

    @Override
    public ResponseEntity<PartResponse> commitReservation(
            UUID partId, CommitPartReservationRequest commitPartReservationRequest) {
        Part part = commitPartReservationUseCase.execute(new CommitPartReservationUseCase.Command(
                partId, commitPartReservationRequest.getQuantity(), commitPartReservationRequest.getReason()));
        return ResponseEntity.ok(PartRestMapper.toResponse(part));
    }
}
