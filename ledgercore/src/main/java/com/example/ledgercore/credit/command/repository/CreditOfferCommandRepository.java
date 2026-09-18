package com.example.ledgercore.credit.command.repository;

import com.example.ledgercore.credit.entity.CreditOffer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CreditOfferCommandRepository
        extends JpaRepository<CreditOffer, UUID> {
}