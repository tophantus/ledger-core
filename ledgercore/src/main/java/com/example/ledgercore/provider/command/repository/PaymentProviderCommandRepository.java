package com.example.ledgercore.provider.command.repository;

import com.example.ledgercore.provider.entity.PaymentProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PaymentProviderCommandRepository
        extends JpaRepository<PaymentProvider, UUID> {

    boolean existsByCode(String code);

    boolean existsByClientId(String clientId);
}