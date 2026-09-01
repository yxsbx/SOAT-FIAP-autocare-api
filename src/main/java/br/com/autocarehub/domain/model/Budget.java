package br.com.autocarehub.domain.model;

import br.com.autocarehub.domain.exception.DomainException;
import br.com.autocarehub.domain.valueobject.Money;
import java.util.List;
import java.util.Objects;

public class Budget {

    private final Money totalAmount;

    public Budget(List<BudgetItem> items) {
        List<BudgetItem> budgetItems = List.copyOf(Objects.requireNonNull(items, "items are required"));
        if (budgetItems.isEmpty()) {
            throw new DomainException("Budget requires at least one item");
        }
        totalAmount = budgetItems.stream().map(BudgetItem::totalPrice).reduce(Money.zero(), Money::add);
    }

    public Money totalAmount() {
        return totalAmount;
    }
}
