package com.example.ledgercore.reconciliation.query.repository;

import com.example.ledgercore.reconciliation.entity.ReconciliationException;
import com.example.ledgercore.reconciliation.enums.ReconciliationErrorCode;
import com.example.ledgercore.reconciliation.enums.ReconciliationTargetType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.UUID;

public interface ReconciliationExceptionQueryRepository
        extends JpaRepository<ReconciliationException, UUID>,
        JpaSpecificationExecutor<ReconciliationException> {
}