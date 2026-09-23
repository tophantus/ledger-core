package com.example.ledgercore.credit.command.repository;

import com.example.ledgercore.credit.entity.CreditDailyBalance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface CreditDailyBalanceCommandRepository
        extends JpaRepository<CreditDailyBalance, UUID> {

    Optional<CreditDailyBalance> findByCreditFacilityIdAndBusinessDate(
            UUID creditFacilityId,
            LocalDate businessDate
    );
}