package com.example.ledgercore.credit.query.handler;

import com.example.ledgercore.common.dto.PageResponse;
import com.example.ledgercore.credit.entity.CreditFacility;
import com.example.ledgercore.credit.entity.CreditStatement;
import com.example.ledgercore.credit.query.dto.GetCreditStatementResponse;
import com.example.ledgercore.credit.query.dto.GetCreditStatementsQuery;
import com.example.ledgercore.credit.query.mapper.CreditStatementQueryMapper;
import com.example.ledgercore.credit.query.port.inbound.GetCreditStatementsUseCase;
import com.example.ledgercore.credit.query.repository.CreditFacilityQueryRepository;
import com.example.ledgercore.credit.query.repository.CreditStatementQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetCreditStatementsHandler
        implements GetCreditStatementsUseCase {

    private final CreditFacilityQueryRepository creditFacilityQueryRepository;
    private final CreditStatementQueryRepository creditStatementQueryRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<GetCreditStatementResponse> execute(
            GetCreditStatementsQuery query
    ) {
        CreditFacility facility =
                creditFacilityQueryRepository
                        .findByIdAndCustomerId(
                                query.creditFacilityId(),
                                query.customerId()
                        )
                        .orElseThrow(() -> new IllegalStateException(
                                "Credit facility not found: "
                                        + query.creditFacilityId()
                        ));

        Page<CreditStatement> statements =
                creditStatementQueryRepository
                        .findByCreditFacilityIdOrderByPeriodEndDesc(
                                facility.getId(),
                                PageRequest.of(query.page(), query.size())
                        );

        return new PageResponse<>(
                statements.getContent()
                        .stream()
                        .map(CreditStatementQueryMapper::toResponse)
                        .toList(),
                statements.getNumber(),
                statements.getSize(),
                statements.getTotalElements(),
                statements.getTotalPages()
        );
    }
}