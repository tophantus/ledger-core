package com.example.ledgercore.withdrawal.command.handler;

import com.example.ledgercore.common.currency.Currency;
import com.example.ledgercore.common.currency.CurrencyAmountPolicy;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.withdrawal.command.dto.CreateWithdrawalRequestCommand;
import com.example.ledgercore.withdrawal.command.dto.WithdrawalRequestResponse;
import com.example.ledgercore.withdrawal.command.port.inbound.CreateWithdrawalRequestUseCase;
import com.example.ledgercore.withdrawal.command.port.outbound.WithdrawalAccountInfo;
import com.example.ledgercore.withdrawal.command.port.outbound.WithdrawalAccountPort;
import com.example.ledgercore.withdrawal.command.port.outbound.WithdrawalOtpPort;
import com.example.ledgercore.withdrawal.command.repository.WithdrawalRequestCommandRepository;
import com.example.ledgercore.withdrawal.config.WithdrawalRequestProperties;
import com.example.ledgercore.withdrawal.entity.WithdrawalRequest;
import com.example.ledgercore.withdrawal.enums.WithdrawalRequestStatus;
import com.example.ledgercore.withdrawal.policy.WithdrawalAmountPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class CreateWithdrawalRequestHandler
        implements CreateWithdrawalRequestUseCase {

    private final WithdrawalRequestCommandRepository withdrawalRequestRepository;
    private final WithdrawalAccountPort withdrawalAccountPort;
    private final WithdrawalOtpPort withdrawalOtpPort;
    private final Clock clock;

    private final WithdrawalRequestProperties withdrawalRequestProperties;

    @Override
    @Transactional
    public WithdrawalRequestResponse execute(
            CreateWithdrawalRequestCommand command
    ) {
        validateAmount(command.amount(), command.currency());

        WithdrawalAccountInfo account =
                withdrawalAccountPort.getWithdrawalInfo(
                        command.accountId()
                );

        if (!account.userId().equals(command.userId())) {
            throw new BusinessException(
                    ErrorCode.ACCESS_DENIED
            );
        }

        if (!account.currency().equals(command.currency())) {
            throw new BusinessException(
                    ErrorCode.ACCOUNT_CURRENCY_MISMATCH
            );
        }

        if (account.availableBalance().compareTo(command.amount()) < 0) {
            throw new BusinessException(
                    ErrorCode.ACCOUNT_INSUFFICIENT_BALANCE
            );
        }

        Instant now = Instant.now(clock);

        Instant expiresAt = now.plus(
                withdrawalRequestProperties.getExpiration()
        );

        WithdrawalRequest request = WithdrawalRequest.builder()
                .userId(command.userId())
                .accountId(command.accountId())
                .amount(command.amount())
                .currency(command.currency())
                .status(WithdrawalRequestStatus.PENDING)
                .expiresAt(expiresAt)
                .createdAt(now)
                .version(0L)
                .build();

        request = withdrawalRequestRepository.save(request);

        withdrawalOtpPort.sendConfirmationOtp(
                request.getUserId(),
                request.getId()
        );

        return new WithdrawalRequestResponse(
                request.getId(),
                request.getAccountId(),
                request.getAmount(),
                request.getCurrency(),
                request.getStatus(),
                request.getExpiresAt()
        );
    }

    private void validateAmount(BigDecimal amount, Currency currency) {
        if (amount == null
                || amount.signum() <= 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_WITHDRAW_AMOUNT
            );
        }

        CurrencyAmountPolicy.validate(
                amount,
                currency
        );

        WithdrawalAmountPolicy.validate(
                amount,
                currency
        );
    }
}