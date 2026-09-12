package com.example.ledgercore.interest.query.mapper;

import com.example.ledgercore.interest.entity.InterestRun;
import com.example.ledgercore.interest.query.dto.InterestRunResponse;

public final class InterestRunQueryMapper {

    private InterestRunQueryMapper() {
    }

    public static InterestRunResponse toResponse(
            InterestRun run
    ) {
        return new InterestRunResponse(
                run.getId(),
                run.getBusinessDate(),
                run.getRunType(),
                run.getStatus(),
                run.getProcessedCount(),
                run.getStartedAt(),
                run.getCompletedAt(),
                run.getCreatedAt()
        );
    }
}