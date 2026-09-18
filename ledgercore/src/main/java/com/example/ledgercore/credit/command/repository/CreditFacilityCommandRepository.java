package com.example.ledgercore.credit.command.repository;

import com.example.ledgercore.credit.entity.CreditFacility;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CreditFacilityCommandRepository
        extends JpaRepository<CreditFacility, UUID> {
}