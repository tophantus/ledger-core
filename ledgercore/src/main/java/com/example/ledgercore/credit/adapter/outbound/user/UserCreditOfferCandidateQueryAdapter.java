package com.example.ledgercore.credit.adapter.outbound.user;

import com.example.ledgercore.credit.command.port.outbound.CreditOfferCandidateQueryPort;
import com.example.ledgercore.credit.command.port.outbound.dto.CreditOfferCandidate;
import com.example.ledgercore.credit.enums.CreditOfferStatus;
import com.example.ledgercore.credit.query.repository.CreditOfferQueryRepository;
import com.example.ledgercore.user.query.dto.GetUserBatchQuery;
import com.example.ledgercore.user.query.dto.GetUserBatchResult;
import com.example.ledgercore.user.query.port.inbound.GetUserBatchUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserCreditOfferCandidateQueryAdapter
        implements CreditOfferCandidateQueryPort {

    private final GetUserBatchUseCase getUserBatchUseCase;

    private final CreditOfferQueryRepository
            creditOfferQueryRepository;

    @Override
    public List<CreditOfferCandidate> findBatch(
            LocalDate businessDate,
            UUID lastProcessedId,
            int batchSize
    ) {
        List<UUID> customerIds =
                getUserBatchUseCase
                        .execute(
                                new GetUserBatchQuery(
                                        lastProcessedId,
                                        batchSize
                                )
                        )
                        .users()
                        .stream()
                        .map(GetUserBatchResult.UserInfo::userId
                        )
                        .toList();

        if (customerIds.isEmpty()) {
            return List.of();
        }

        Set<UUID> customerIdsWithActiveOffer =
                new HashSet<>(
                        creditOfferQueryRepository
                                .findCustomerIdsWithActiveOffer(
                                        customerIds,
                                        CreditOfferStatus.OFFERED,
                                        Instant.now()
                                )
                );

        return customerIds.stream()
                .filter(customerId ->
                        !customerIdsWithActiveOffer.contains(
                                customerId
                        )
                )
                .map(CreditOfferCandidate::new)
                .toList();
    }
}