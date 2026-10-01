package com.example.ledgercore.transaction.adapter.outbound.credit;

import com.example.ledgercore.credit.query.dto.GetUserCreditFacilityQuery;
import com.example.ledgercore.credit.query.dto.GetUserCreditFacilityResult;
import com.example.ledgercore.credit.query.port.inbound.GetUserCreditFacilityUseCase;
import com.example.ledgercore.transaction.query.port.outbound.TransactionCreditFacilityQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TransactionCreditFacilityQueryAdapter
        implements TransactionCreditFacilityQueryPort {

    private final GetUserCreditFacilityUseCase
            getUserCreditFacilityUseCase;

    @Override
    public Optional<UUID> findCreditFacilityIdByUserId(
            UUID userId
    ) {
        return getUserCreditFacilityUseCase.execute(
                new GetUserCreditFacilityQuery(userId)
        ).map(GetUserCreditFacilityResult::id);
    }
}