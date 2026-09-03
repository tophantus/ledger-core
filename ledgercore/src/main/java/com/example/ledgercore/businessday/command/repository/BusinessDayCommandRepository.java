package com.example.ledgercore.businessday.command.repository;

import com.example.ledgercore.businessday.entity.BusinessDay;
import com.example.ledgercore.businessday.enums.BusinessDayStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface BusinessDayCommandRepository
        extends JpaRepository<BusinessDay, LocalDate> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select b
            from BusinessDay b
            where b.status = :status
            """)
    Optional<BusinessDay> findByStatusForUpdate(
            @Param("status") BusinessDayStatus status
    );

    boolean existsById(LocalDate businessDate);
}