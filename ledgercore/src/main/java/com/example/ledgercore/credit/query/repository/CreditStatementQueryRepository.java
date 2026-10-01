package com.example.ledgercore.credit.query.repository;

import com.example.ledgercore.credit.entity.CreditStatement;
import com.example.ledgercore.credit.enums.CreditRepaymentMandateStatus;
import com.example.ledgercore.credit.enums.CreditStatementStatus;
import com.example.ledgercore.credit.enums.RepaymentType;
import com.example.ledgercore.credit.query.dto.GetDueRepaymentCandidatesResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface CreditStatementQueryRepository
        extends JpaRepository<CreditStatement, UUID> {

    Page<CreditStatement> findByCreditFacilityIdOrderByPeriodEndDesc(
            UUID creditFacilityId,
            Pageable pageable
    );

    @Query("""
        select new com.example.ledgercore.credit.query.dto.GetDueRepaymentCandidatesResult(
            s.id,
            s.creditFacilityId,
            m.accountId,
            m.repaymentType
        )
        from CreditStatement s
        join CreditRepaymentMandate m
            on m.creditFacilityId = s.creditFacilityId
        where m.status = :mandateStatus
          and s.status in :statementStatuses
          and (
              s.nextRepaymentAttemptAt is null
              or s.nextRepaymentAttemptAt <= :now
          )
          and (
              (
                  m.repaymentType = :fullBalance
                  and s.paidAmount < s.closingBalance
              )
              or
              (
                  m.repaymentType = :minimumPayment
                  and s.paidAmount < s.minimumPayment
              )
          )
        order by s.id
        """)
    List<GetDueRepaymentCandidatesResult> findDueRepaymentCandidates(
            @Param("mandateStatus") CreditRepaymentMandateStatus mandateStatus,
            @Param("statementStatuses") List<CreditStatementStatus> statementStatuses,
            @Param("fullBalance") RepaymentType fullBalance,
            @Param("minimumPayment") RepaymentType minimumPayment,
            @Param("now") Instant now,
            Pageable pageable
    );
}