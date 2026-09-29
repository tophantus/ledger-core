package com.example.ledgercore.credit.command.handler;

import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.credit.command.dto.CreateCreditRepaymentMandateCommand;
import com.example.ledgercore.credit.command.dto.CreateCreditRepaymentMandateResult;
import com.example.ledgercore.credit.command.port.inbound.CreateCreditRepaymentMandateUseCase;
import com.example.ledgercore.credit.command.port.outbound.VerifyRepaymentAccountOwnershipPort;
import com.example.ledgercore.credit.command.repository.CreditRepaymentMandateCommandRepository;
import com.example.ledgercore.credit.entity.CreditRepaymentMandate;
import com.example.ledgercore.credit.enums.CreditRepaymentMandateStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateCreditRepaymentMandateHandler
        implements CreateCreditRepaymentMandateUseCase {

    private final CreditRepaymentMandateCommandRepository mandateRepository;
    private final VerifyRepaymentAccountOwnershipPort ownershipPort;

    @Override
    @Transactional
    public CreateCreditRepaymentMandateResult execute(
            CreateCreditRepaymentMandateCommand command
    ) {
        validateCommand(command);

        if (!ownershipPort.verify(
                command.userId(),
                command.accountId()
        )) {
            throw new BusinessException(
                    ErrorCode.ACCESS_DENIED
            );
        }

        if (mandateRepository.existsByCreditFacilityIdAndAccountIdAndStatus(
                command.creditFacilityId(),
                command.accountId(),
                CreditRepaymentMandateStatus.ACTIVE
        )) {
            throw new BusinessException(
                    ErrorCode.CREDIT_REPAYMENT_MANDATE_ALREADY_EXISTS
            );
        }

        Instant now = Instant.now();
        UUID mandateId = UUID.randomUUID();

        CreditRepaymentMandate mandate =
                CreditRepaymentMandate.builder()
                        .id(mandateId)
                        .creditFacilityId(command.creditFacilityId())
                        .accountId(command.accountId())
                        .repaymentType(command.repaymentType())
                        .status(CreditRepaymentMandateStatus.ACTIVE)
                        .createdAt(now)
                        .build();

        mandateRepository.save(mandate);

        return new CreateCreditRepaymentMandateResult(
                mandate.getId(),
                mandate.getCreditFacilityId(),
                mandate.getAccountId(),
                mandate.getRepaymentType(),
                mandate.getStatus(),
                mandate.getCreatedAt()
        );
    }

    private void validateCommand(
            CreateCreditRepaymentMandateCommand command
    ) {
        if (command == null
                || command.userId() == null
                || command.creditFacilityId() == null
                || command.accountId() == null
                || command.repaymentType() == null) {

            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
    }
}