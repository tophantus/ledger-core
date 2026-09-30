package com.example.ledgercore.credit.command.handler;

import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.credit.command.dto.UpdateCreditRepaymentMandateCommand;
import com.example.ledgercore.credit.command.dto.UpdateCreditRepaymentMandateResult;
import com.example.ledgercore.credit.command.port.inbound.UpdateCreditRepaymentMandateUseCase;
import com.example.ledgercore.credit.command.port.outbound.VerifyRepaymentAccountOwnershipPort;
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
public class UpdateCreditRepaymentMandateHandler
        implements UpdateCreditRepaymentMandateUseCase {

    private final CreditRepaymentMandateCommandRepository mandateRepository;
    private final CreditFacilityCommandRepository creditFacilityRepository;
    private final VerifyRepaymentAccountOwnershipPort ownershipPort;

    @Override
    @Transactional
    public UpdateCreditRepaymentMandateResult execute(
            UpdateCreditRepaymentMandateCommand command
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

        if (command.accountId() != null
                && !command.accountId().equals(mandate.getAccountId())
                && !ownershipPort.verify(
                command.userId(),
                command.accountId()
        )) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        mandate.update(
                command.accountId(),
                command.repaymentType()
        );

        mandateRepository.save(mandate);

        return new UpdateCreditRepaymentMandateResult(
                mandate.getId(),
                creditFacilityId,
                mandate.getAccountId(),
                mandate.getRepaymentType(),
                mandate.getStatus(),
                mandate.getUpdatedAt()
        );
    }

    private void validate(UpdateCreditRepaymentMandateCommand command) {
        if (command == null
                || command.userId() == null
                || command.mandateId() == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
    }
}