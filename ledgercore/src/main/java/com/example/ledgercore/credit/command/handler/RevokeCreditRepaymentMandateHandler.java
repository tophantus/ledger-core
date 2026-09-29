package com.example.ledgercore.credit.command.handler;

import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.credit.command.dto.RevokeCreditRepaymentMandateCommand;
import com.example.ledgercore.credit.command.dto.RevokeCreditRepaymentMandateResult;
import com.example.ledgercore.credit.command.port.inbound.RevokeCreditRepaymentMandateUseCase;
import com.example.ledgercore.credit.command.repository.CreditFacilityCommandRepository;
import com.example.ledgercore.credit.command.repository.CreditRepaymentMandateCommandRepository;
import com.example.ledgercore.credit.entity.CreditFacility;
import com.example.ledgercore.credit.entity.CreditRepaymentMandate;
import com.example.ledgercore.credit.enums.CreditRepaymentMandateStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RevokeCreditRepaymentMandateHandler
        implements RevokeCreditRepaymentMandateUseCase {

    private final CreditRepaymentMandateCommandRepository mandateRepository;
    private final CreditFacilityCommandRepository creditFacilityRepository;

    @Override
    @Transactional
    public RevokeCreditRepaymentMandateResult execute(
            RevokeCreditRepaymentMandateCommand command
    ) {
        validate(command);

        CreditRepaymentMandate mandate = mandateRepository
                .findById(command.mandateId())
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.CREDIT_REPAYMENT_MANDATE_NOT_FOUND
                ));

        UUID creditFacilityId = mandate.getCreditFacilityId();

        CreditFacility creditFacility = creditFacilityRepository
                .findById(creditFacilityId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.CREDIT_FACILITY_NOT_FOUND
                ));

        if (!creditFacility.getCustomerId().equals(command.userId())) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        if (mandate.getStatus() != CreditRepaymentMandateStatus.ACTIVE) {
            throw new BusinessException(
                    ErrorCode.CREDIT_REPAYMENT_MANDATE_NOT_ACTIVE
            );
        }

        mandate.revoke();

        mandateRepository.save(mandate);

        return new RevokeCreditRepaymentMandateResult(
                mandate.getId(),
                creditFacilityId,
                mandate.getAccountId(),
                mandate.getStatus(),
                mandate.getRevokedAt()
        );
    }

    private void validate(RevokeCreditRepaymentMandateCommand command) {
        if (command == null
                || command.userId() == null
                || command.mandateId() == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
    }
}