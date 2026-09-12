package com.example.ledgercore.transaction.query.specification;

import com.example.ledgercore.common.currency.Currency;
import com.example.ledgercore.transaction.entity.MoneyTransaction;
import com.example.ledgercore.transaction.enums.TransactionStatus;
import com.example.ledgercore.transaction.enums.TransactionType;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class TransactionSpecifications {

    private TransactionSpecifications() {
    }

    public static Specification<MoneyTransaction> account(
            UUID accountId
    ) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.or(
                        criteriaBuilder.equal(
                                root.get("sourceAccountId"),
                                accountId
                        ),
                        criteriaBuilder.equal(
                                root.get("destinationAccountId"),
                                accountId
                        )
                );
    }

    public static Specification<MoneyTransaction> accounts(
            List<UUID> accountIds
    ) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.or(
                        root.get("sourceAccountId")
                                .in(accountIds),
                        root.get("destinationAccountId")
                                .in(accountIds)
                );
    }

    public static Specification<MoneyTransaction> status(
            TransactionStatus status
    ) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(
                        root.get("status"),
                        status
                );
    }

    public static Specification<MoneyTransaction> type(
            TransactionType type
    ) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(
                        root.get("type"),
                        type
                );
    }

    public static Specification<MoneyTransaction> currency(
            Currency currency
    ) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(
                        root.get("currency"),
                        currency
                );
    }

    public static Specification<MoneyTransaction> createdAtFrom(
            Instant from
    ) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThanOrEqualTo(
                        root.get("createdAt"),
                        from
                );
    }

    public static Specification<MoneyTransaction> createdAtTo(
            Instant to
    ) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.lessThanOrEqualTo(
                        root.get("createdAt"),
                        to
                );
    }
}