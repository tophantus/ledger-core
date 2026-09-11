package com.example.ledgercore.interest.command.handler;

import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.interest.command.dto.ClaimedInterestRun;
import com.example.ledgercore.interest.command.port.inbound.ClaimInterestRunUseCase;
import com.example.ledgercore.interest.command.repository.InterestRunCommandRepository;
import com.example.ledgercore.interest.config.InterestRunProperties;
import com.example.ledgercore.interest.entity.InterestRun;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ClaimInterestRunHandler
        implements ClaimInterestRunUseCase {

    private final InterestRunCommandRepository interestRunCommandRepository;
    private final InterestRunProperties properties;

    @Override
    @Transactional
    public Optional<ClaimedInterestRun> execute(Instant claimAt) {

        if (claimAt == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        Instant staleBefore =
                claimAt.minus(properties.getLeaseDuration());

        Optional<InterestRun> optionalRun =
                interestRunCommandRepository.findClaimableRun(
                        staleBefore
                );

        if (optionalRun.isEmpty()) {
            return Optional.empty();
        }

        InterestRun run = optionalRun.get();

        run.start(claimAt);

        interestRunCommandRepository.save(run);

        return Optional.of(
                new ClaimedInterestRun(
                        run.getId(),
                        run.getBusinessDate(),
                        run.getRunType(),
                        run.getLastProcessedId(),
                        run.getProcessedCount()
                )
        );
    }
}