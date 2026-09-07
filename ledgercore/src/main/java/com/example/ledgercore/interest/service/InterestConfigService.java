package com.example.ledgercore.interest.service;

import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.interest.entity.InterestConfig;
import com.example.ledgercore.interest.query.repository.InterestConfigQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class InterestConfigService {

    private final InterestConfigQueryRepository repository;

    public InterestConfig getApplicableConfig(
            String productCode,
            String currency,
            LocalDate businessDate
    ) {
        return repository.findApplicableConfig(
                        productCode,
                        currency,
                        businessDate
                )
                .orElseThrow(() ->
                        new BusinessException(
                                ErrorCode.INTEREST_CONFIG_NOT_FOUND
                        )
                );
    }
}