package com.example.ledgercore.card.command.repository;

import com.example.ledgercore.card.entity.CardCapture;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CardCaptureCommandRepository
        extends JpaRepository<CardCapture, UUID> {
}
