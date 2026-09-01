package br.com.autocarehub.domain.model;

import br.com.autocarehub.domain.exception.DomainException;
import br.com.autocarehub.domain.service.DomainValidation;
import br.com.autocarehub.domain.valueobject.Money;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

public class Part {

    private static final int NAME_MAX_LENGTH = 120;
    private static final int DESCRIPTION_MAX_LENGTH = 500;
    private static final int SKU_MAX_LENGTH = 60;
    private static final int CATEGORY_MAX_LENGTH = 80;
    private static final int BRAND_MAX_LENGTH = 80;
    private static final int DEFAULT_RESERVATION_DAYS = 3;

    private final UUID id;
    private String name;
    private String description;
    private String sku;
    private String category;
    private @Nullable String subcategory;
    private String brand;
    private Money costPrice;
    private Money unitPrice;
    private int stockQuantity;
    private int reservedQuantity;
    private int minimumStock;
    private int reservationDays;
    private @Nullable LocalDateTime reservationExpiresAt;
    private boolean active;

    private Part(UUID id, CatalogData catalogData, Pricing pricing, StockState stockState, ActivationStatus status) {
        this.id = Objects.requireNonNull(id, "id is required");
        this.name = DomainValidation.requireText(catalogData.name(), "Name is required", NAME_MAX_LENGTH);
        this.description = DomainValidation.requireText(
                catalogData.description(), "Description is required", DESCRIPTION_MAX_LENGTH);
        this.sku = DomainValidation.requireText(catalogData.sku(), "SKU is required", SKU_MAX_LENGTH);
        this.category =
                DomainValidation.requireText(catalogData.category(), "Category is required", CATEGORY_MAX_LENGTH);
        this.subcategory = DomainValidation.optionalText(catalogData.subcategory());
        this.brand = DomainValidation.requireText(catalogData.brand(), "Brand is required", BRAND_MAX_LENGTH);
        this.costPrice = requireNonNegativeMoney(pricing.costPrice());
        this.unitPrice = requirePositiveMoney(pricing.unitPrice());
        this.stockQuantity = requireNonNegative(stockState.stockQuantity(), "Stock cannot be negative");
        this.reservedQuantity = requireNonNegative(stockState.reservedQuantity(), "Reserved stock cannot be negative");
        if (this.reservedQuantity > this.stockQuantity) {
            throw new DomainException("Reserved stock cannot be greater than stock");
        }
        this.minimumStock = requireNonNegative(stockState.minimumStock(), "Minimum stock cannot be negative");
        this.reservationDays = normalizeReservationDays(stockState.reservationDays());
        this.reservationExpiresAt = stockState.reservationExpiresAt();
        this.active = status.active();
    }

    public static Part create(CatalogData catalogData, Pricing pricing, int stockQuantity, int minimumStock) {
        return new Part(
                UUID.randomUUID(),
                catalogData,
                pricing,
                StockState.initial(stockQuantity, minimumStock),
                ActivationStatus.ACTIVE);
    }

    public static Part restore(
            UUID id, CatalogData catalogData, Pricing pricing, StockState stockState, ActivationStatus status) {
        return new Part(id, catalogData, pricing, stockState, status);
    }

    private static Money requireNonNegativeMoney(Money money) {
        if (money.value().signum() < 0) {
            throw new DomainException("Cost price cannot be negative");
        }
        return money;
    }

    private static Money requirePositiveMoney(Money money) {
        Objects.requireNonNull(money, "money is required");
        if (money.isZeroOrNegative()) {
            throw new DomainException("Unit price must be greater than zero");
        }
        return money;
    }

    private static int requireNonNegative(int value, String message) {
        if (value < 0) {
            throw new DomainException(message);
        }
        return value;
    }

    private static int normalizeReservationDays(int value) {
        if (value <= 0) {
            return DEFAULT_RESERVATION_DAYS;
        }
        return value;
    }

    public void update(
            String newName,
            String newDescription,
            String newSku,
            String newCategory,
            @Nullable String newSubcategory,
            String newBrand,
            Money newCostPrice,
            Money newUnitPrice,
            int newMinimumStock) {
        name = DomainValidation.requireText(newName, "Name is required", NAME_MAX_LENGTH);
        description = DomainValidation.requireText(newDescription, "Description is required", DESCRIPTION_MAX_LENGTH);
        sku = DomainValidation.requireText(newSku, "SKU is required", SKU_MAX_LENGTH);
        category = DomainValidation.requireText(newCategory, "Category is required", CATEGORY_MAX_LENGTH);
        subcategory = DomainValidation.optionalText(newSubcategory);
        brand = DomainValidation.requireText(newBrand, "Brand is required", BRAND_MAX_LENGTH);
        costPrice = requireNonNegativeMoney(newCostPrice);
        unitPrice = requirePositiveMoney(newUnitPrice);
        minimumStock = requireNonNegative(newMinimumStock, "Minimum stock cannot be negative");
    }

    public void update(
            String newName,
            String newDescription,
            String newSku,
            String newCategory,
            @Nullable String newSubcategory,
            String newBrand,
            Money newUnitPrice,
            int newMinimumStock) {
        update(
                newName,
                newDescription,
                newSku,
                newCategory,
                newSubcategory,
                newBrand,
                costPrice,
                newUnitPrice,
                newMinimumStock);
    }

    public void update(
            String newName,
            String newSku,
            String newCategory,
            @Nullable String newSubcategory,
            String newBrand,
            Money newUnitPrice,
            int newMinimumStock) {
        update(
                newName,
                newName,
                newSku,
                newCategory,
                newSubcategory,
                newBrand,
                costPrice,
                newUnitPrice,
                newMinimumStock);
    }

    public void update(
            String newName,
            String newSku,
            String newCategory,
            @Nullable String newSubcategory,
            String newBrand,
            Money newCostPrice,
            Money newUnitPrice,
            int newMinimumStock) {
        update(
                newName,
                newName,
                newSku,
                newCategory,
                newSubcategory,
                newBrand,
                newCostPrice,
                newUnitPrice,
                newMinimumStock);
    }

    public void increaseStock(int quantity) {
        if (quantity <= 0) {
            throw new DomainException("Quantity must be greater than zero");
        }
        this.stockQuantity += quantity;
    }

    public void reduceStock(int quantity) {
        if (quantity <= 0) {
            throw new DomainException("Quantity must be greater than zero");
        }
        if (quantity > availableQuantity()) {
            throw new DomainException("Insufficient stock");
        }
        this.stockQuantity -= quantity;
    }

    public boolean hasAvailableStock(int quantity) {
        releaseExpiredReservation();
        return quantity > 0 && availableQuantity() >= quantity;
    }

    /**
     * Keeps requested items unavailable to other service orders until the reservation expires or is explicitly
     * released.
     */
    public void reserveStock(int quantity) {
        if (quantity <= 0) {
            throw new DomainException("Quantity must be greater than zero");
        }
        releaseExpiredReservation();
        if (quantity > availableQuantity()) {
            throw new DomainException("Insufficient stock");
        }
        this.reservedQuantity += quantity;
        this.reservationExpiresAt = LocalDateTime.now().plusDays(reservationDays);
    }

    /**
     * Confirms stock consumption prioritizing previously reserved quantity, then consuming remaining available stock.
     */
    public void commitReservedStock(int quantity) {
        if (quantity <= 0) {
            throw new DomainException("Quantity must be greater than zero");
        }
        releaseExpiredReservation();
        int quantityToCommit = Math.min(quantity, reservedQuantity);
        int remainingQuantity = quantity - quantityToCommit;
        if (remainingQuantity > availableQuantity()) {
            throw new DomainException("Insufficient stock");
        }
        this.reservedQuantity -= quantityToCommit;
        this.stockQuantity -= quantity;
        if (reservedQuantity == 0) {
            this.reservationExpiresAt = null;
        }
    }

    public void releaseReservedStock(int quantity) {
        if (quantity <= 0) {
            throw new DomainException("Quantity must be greater than zero");
        }
        this.reservedQuantity = Math.max(0, reservedQuantity - quantity);
        if (reservedQuantity == 0) {
            this.reservationExpiresAt = null;
        }
    }

    /**
     * Reservation expiration is evaluated lazily to avoid background jobs for short-lived budget reservations.
     */
    public void releaseExpiredReservation() {
        if (reservationExpiresAt != null && reservationExpiresAt.isBefore(LocalDateTime.now())) {
            reservedQuantity = 0;
            reservationExpiresAt = null;
        }
    }

    public void configureReservationDays(int newReservationDays) {
        if (newReservationDays <= 0) {
            throw new DomainException("Reservation days must be greater than zero");
        }
        reservationDays = newReservationDays;
    }

    public int availableQuantity() {
        releaseExpiredReservation();
        return Math.max(0, stockQuantity - reservedQuantity);
    }

    public String stockStatus() {
        if (!active) {
            return "INACTIVE";
        }
        if (availableQuantity() <= 0) {
            return "OUT_OF_STOCK";
        }
        if (availableQuantity() <= minimumStock) {
            return "LOW_STOCK";
        }
        if (reservedQuantity > 0) {
            return "RESERVED";
        }
        return "AVAILABLE";
    }

    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }

    public UUID id() {
        return id;
    }

    public String name() {
        return name;
    }

    public String description() {
        return description;
    }

    public String sku() {
        return sku;
    }

    public String category() {
        return category;
    }

    public @Nullable String subcategory() {
        return subcategory;
    }

    public String brand() {
        return brand;
    }

    public Money unitPrice() {
        return unitPrice;
    }

    public Money costPrice() {
        return costPrice;
    }

    public int stockQuantity() {
        return stockQuantity;
    }

    public int reservedQuantity() {
        return reservedQuantity;
    }

    public int minimumStock() {
        return minimumStock;
    }

    public int reservationDays() {
        return reservationDays;
    }

    public @Nullable LocalDateTime reservationExpiresAt() {
        return reservationExpiresAt;
    }

    public boolean active() {
        return active;
    }

    public enum ActivationStatus {
        ACTIVE(true),
        INACTIVE(false);

        private final boolean active;

        ActivationStatus(boolean active) {
            this.active = active;
        }

        public static ActivationStatus fromActive(boolean active) {
            return active ? ACTIVE : INACTIVE;
        }

        private boolean active() {
            return active;
        }
    }

    public record CatalogData(
            String name, String description, String sku, String category, @Nullable String subcategory, String brand) {}

    public record Pricing(Money costPrice, Money unitPrice) {

        public static Pricing withoutCost(Money unitPrice) {
            return new Pricing(Money.zero(), unitPrice);
        }
    }

    public record StockState(
            int stockQuantity,
            int reservedQuantity,
            int minimumStock,
            int reservationDays,
            @Nullable LocalDateTime reservationExpiresAt) {

        public static StockState initial(int stockQuantity, int minimumStock) {
            return new StockState(stockQuantity, 0, minimumStock, DEFAULT_RESERVATION_DAYS, null);
        }
    }
}
