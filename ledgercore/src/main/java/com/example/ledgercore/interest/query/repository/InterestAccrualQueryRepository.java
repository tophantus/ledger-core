package com.example.ledgercore.interest.query.repository;

import com.example.ledgercore.interest.entity.InterestAccrual;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface InterestAccrualQueryRepository
        extends JpaRepository<InterestAccrual, UUID> {

    List<InterestAccrual> findByAccountIdAndBusinessDateBetweenAndPostingIdIsNull(
            UUID accountId,
            LocalDate periodStart,
            LocalDate periodEnd
    );
}