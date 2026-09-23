package com.example.ledgercore.provider.query.repository;

import com.example.ledgercore.provider.entity.PaymentProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PaymentProviderQueryRepository
        extends JpaRepository<PaymentProvider, UUID> {
}