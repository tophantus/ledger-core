package com.example.ledgercore.interest.query.repository;

import com.example.ledgercore.interest.entity.InterestPosting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface InterestPostingQueryRepository
        extends JpaRepository<InterestPosting, UUID> {

    List<InterestPosting> findByPeriodStartAndPeriodEnd(
            LocalDate periodStart,
            LocalDate periodEnd
    );

    boolean existsByAccountIdAndPeriodStartAndPeriodEnd(
            UUID accountId,
            LocalDate periodStart,
            LocalDate periodEnd
    );
}