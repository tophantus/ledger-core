package com.example.ledgercore.interest.command.repository;

import com.example.ledgercore.interest.entity.InterestRun;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface InterestRunCommandRepository
        extends JpaRepository<InterestRun, UUID> {

    Optional<InterestRun> findByBusinessDate(
            LocalDate businessDate
    );

    boolean existsByBusinessDate(
            LocalDate businessDate
    );
}