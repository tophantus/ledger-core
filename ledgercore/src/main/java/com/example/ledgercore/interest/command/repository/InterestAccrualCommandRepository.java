package com.example.ledgercore.interest.command.repository;

import com.example.ledgercore.interest.entity.InterestAccrual;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InterestAccrualCommandRepository
        extends JpaRepository<InterestAccrual, UUID> {

    Optional<InterestAccrual> findByAccountIdAndBusinessDate(
            UUID accountId,
            LocalDate businessDate
    );

    List<InterestAccrual>
    findByAccountIdAndBusinessDateBetweenAndPostingIdIsNull(
            UUID accountId,
            LocalDate periodStart,
            LocalDate periodEnd
    );
}
