package com.example.ledgercore.interest.query.repository;

import com.example.ledgercore.interest.entity.InterestRun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface InterestRunQueryRepository
        extends JpaRepository<InterestRun, UUID>,
        JpaSpecificationExecutor<InterestRun> {
}