package com.example.ledgercore.interest.query.specification;

import com.example.ledgercore.interest.entity.InterestPosting;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.UUID;

public final class InterestPostingSpecifications {

    private InterestPostingSpecifications() {
    }

    public static Specification<InterestPosting> runId(
            UUID runId
    ) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(
                        root.get("runId"),
                        runId
                );
    }

    public static Specification<InterestPosting> periodEnd(
            LocalDate periodEnd
    ) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(
                        root.get("periodEnd"),
                        periodEnd
                );
    }

    public static Specification<InterestPosting> accountId(
            UUID accountId
    ) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(
                        root.get("accountId"),
                        accountId
                );
    }
}