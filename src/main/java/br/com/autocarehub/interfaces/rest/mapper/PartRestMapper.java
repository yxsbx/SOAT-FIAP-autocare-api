package br.com.autocarehub.interfaces.rest.mapper;

import br.com.autocarehub.application.usecase.part.CreatePartUseCase;
import br.com.autocarehub.application.usecase.part.ListPartsUseCase;
import br.com.autocarehub.application.usecase.part.UpdatePartStockUseCase;
import br.com.autocarehub.application.usecase.part.UpdatePartUseCase;
import br.com.autocarehub.domain.model.Part;
import br.com.autocarehub.domain.valueobject.Money;
import br.com.autocarehub.interfaces.rest.generated.model.CreatePartRequest;
import br.com.autocarehub.interfaces.rest.generated.model.PartListResponse;
import br.com.autocarehub.interfaces.rest.generated.model.PartResponse;
import br.com.autocarehub.interfaces.rest.generated.model.UpdatePartRequest;
import br.com.autocarehub.interfaces.rest.generated.model.UpdatePartStockRequest;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public final class PartRestMapper {

    private PartRestMapper() {}

    public static CreatePartUseCase.Command toCommand(CreatePartRequest request) {
        return new CreatePartUseCase.Command(
                request.getName(),
                request.getDescription(),
                request.getSku(),
                request.getCategory(),
                request.getSubcategory(),
                request.getBrand(),
                new Money(BigDecimal.valueOf(request.getCostPrice() == null ? 0 : request.getCostPrice())),
                new Money(BigDecimal.valueOf(request.getUnitPrice())),
                request.getStockQuantity(),
                request.getMinimumStock());
    }

    public static UpdatePartUseCase.Command toCommand(UUID partId, UpdatePartRequest request) {
        return new UpdatePartUseCase.Command(
                partId,
                request.getName(),
                request.getDescription(),
                request.getSku(),
                request.getCategory(),
                request.getSubcategory(),
                request.getBrand(),
                new Money(BigDecimal.valueOf(request.getCostPrice() == null ? 0 : request.getCostPrice())),
                new Money(BigDecimal.valueOf(request.getUnitPrice())),
                request.getStockQuantity(),
                request.getMinimumStock(),
                Boolean.TRUE.equals(request.getActive()));
    }

    public static UpdatePartStockUseCase.Command toCommand(UUID partId, UpdatePartStockRequest request) {
        return new UpdatePartStockUseCase.Command(partId, request.getStockQuantity());
    }

    public static ListPartsUseCase.Query toQuery(Boolean active, Boolean lowStock) {
        return new ListPartsUseCase.Query(active, lowStock);
    }

    public static PartResponse toResponse(Part part) {
        return new PartResponse(
                        part.id(),
                        part.name(),
                        part.description(),
                        part.sku(),
                        part.category(),
                        part.brand(),
                        part.unitPrice().value().doubleValue(),
                        part.costPrice().value().doubleValue(),
                        part.stockQuantity(),
                        part.reservedQuantity(),
                        part.minimumStock(),
                        part.stockStatus(),
                        part.active())
                .subcategory(part.subcategory())
                .availableQuantity(part.availableQuantity())
                .reservationDays(part.reservationDays())
                .reservationExpiresAt(RestMapperSupport.toOffsetDateTime(part.reservationExpiresAt()));
    }

    public static PartListResponse toListResponse(List<Part> parts, Integer page, Integer size) {
        return new PartListResponse(RestMapperSupport.page(parts, page, size).stream()
                .map(PartRestMapper::toResponse)
                .toList());
    }
}
