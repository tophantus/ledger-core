package com.example.ledgercore.interest.command.repository;

import com.example.ledgercore.interest.entity.InterestPosting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.UUID;

public interface InterestPostingCommandRepository
        extends JpaRepository<InterestPosting, UUID> {

    boolean existsByAccountIdAndPeriodStartAndPeriodEnd(
            UUID accountId,
            LocalDate periodStart,
            LocalDate periodEnd
    );
}