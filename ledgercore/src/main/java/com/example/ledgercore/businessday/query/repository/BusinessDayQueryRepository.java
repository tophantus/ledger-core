package com.example.ledgercore.businessday.query.repository;

import com.example.ledgercore.businessday.entity.BusinessDay;
import com.example.ledgercore.businessday.enums.BusinessDayStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BusinessDayQueryRepository
        extends JpaRepository<BusinessDay, java.time.LocalDate> {

    Optional<BusinessDay> findByStatus(
            BusinessDayStatus status
    );
}