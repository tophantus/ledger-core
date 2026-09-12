package com.example.ledgercore.interest.query.specification;

import com.example.ledgercore.common.currency.Currency;
import com.example.ledgercore.interest.entity.InterestAccrual;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.UUID;

public final class InterestAccrualSpecifications {

    private InterestAccrualSpecifications() {
    }

    public static Specification<InterestAccrual> runId(
            UUID runId
    ) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(
                        root.get("runId"),
                        runId
                );
    }

    public static Specification<InterestAccrual> businessDate(
            LocalDate businessDate
    ) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(
                        root.get("businessDate"),
                        businessDate
                );
    }

    public static Specification<InterestAccrual> accountId(
            UUID accountId
    ) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(
                        root.get("accountId"),
                        accountId
                );
    }

    public static Specification<InterestAccrual> currency(
            Currency currency
    ) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(
                        root.get("currency"),
                        currency
                );
    }
}