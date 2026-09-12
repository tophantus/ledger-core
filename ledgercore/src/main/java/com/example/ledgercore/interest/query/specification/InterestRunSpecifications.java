package com.example.ledgercore.interest.query.specification;

import com.example.ledgercore.interest.entity.InterestRun;
import com.example.ledgercore.interest.enums.InterestRunStatus;
import com.example.ledgercore.interest.enums.InterestRunType;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public final class InterestRunSpecifications {

    private InterestRunSpecifications() {
    }

    public static Specification<InterestRun> businessDate(
            LocalDate businessDate
    ) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(
                        root.get("businessDate"),
                        businessDate
                );
    }

    public static Specification<InterestRun> businessDateFrom(
            LocalDate fromDate
    ) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThanOrEqualTo(
                        root.get("businessDate"),
                        fromDate
                );
    }

    public static Specification<InterestRun> businessDateTo(
            LocalDate toDate
    ) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.lessThanOrEqualTo(
                        root.get("businessDate"),
                        toDate
                );
    }

    public static Specification<InterestRun> runType(
            InterestRunType runType
    ) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(
                        root.get("runType"),
                        runType
                );
    }

    public static Specification<InterestRun> status(
            InterestRunStatus status
    ) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(
                        root.get("status"),
                        status
                );
    }
}