package com.example.ledgercore.interest.command.service;

import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.interest.command.dto.ClaimedInterestRun;
import com.example.ledgercore.interest.enums.InterestRunType;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class InterestRunDispatcherImpl
        implements InterestRunDispatcher {

    private final Map<
            InterestRunType,
            InterestRunProcessor
            > processors;

    public InterestRunDispatcherImpl(
            List<InterestRunProcessor> processors
    ) {
        Map<InterestRunType, InterestRunProcessor> map =
                new EnumMap<>(InterestRunType.class);

        for (InterestRunProcessor processor : processors) {

            InterestRunProcessor previous =
                    map.put(
                            processor.getType(),
                            processor
                    );

            if (previous != null) {
                throw new IllegalStateException(
                        "Duplicate interest processor for type: "
                                + processor.getType()
                );
            }
        }

        this.processors = Map.copyOf(map);
    }

    @Override
    public void dispatch(ClaimedInterestRun run) {

        if (run == null) {
            throw new IllegalArgumentException(
                    "run must not be null"
            );
        }

        InterestRunProcessor processor =
                processors.get(run.runType());

        if (processor == null) {
            throw new BusinessException(
                    ErrorCode.INTEREST_PROCESSOR_NOT_FOUND
            );
        }

        processor.process(run);
    }
}