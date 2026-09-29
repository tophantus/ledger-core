package com.example.ledgercore.interest.command.repository;

import com.example.ledgercore.interest.entity.CreditInterestAccrual;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface CreditInterestAccrualCommandRepository
        extends JpaRepository<CreditInterestAccrual, UUID> {

    Optional<CreditInterestAccrual>
    findByCreditFacilityIdAndBusinessDate(
            UUID creditFacilityId,
            LocalDate businessDate
    );
}
