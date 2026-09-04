package com.example.ledgercore.reconciliation.command.service;

import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.reconciliation.entity.ReconciliationRun;
import com.example.ledgercore.reconciliation.enums.ReconciliationType;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class ReconciliationDispatcherImpl
        implements ReconciliationDispatcher {

    private final Map<
            ReconciliationType,
            ReconciliationProcessor
            > processors;

    public ReconciliationDispatcherImpl(
            List<ReconciliationProcessor> processors
    ) {

        Map<ReconciliationType, ReconciliationProcessor> map =
                new EnumMap<>(ReconciliationType.class);

        for (ReconciliationProcessor processor : processors) {

            ReconciliationProcessor previous =
                    map.put(
                            processor.getType(),
                            processor
                    );

            if (previous != null) {
                throw new IllegalStateException(
                        "Duplicate reconciliation processor for type: "
                                + processor.getType()
                );
            }
        }

        this.processors = Map.copyOf(map);
    }

    @Override
    public void dispatch(ReconciliationRun run) {

        ReconciliationProcessor processor =
                processors.get(run.getType());

        if (processor == null) {
            throw new BusinessException(
                    ErrorCode.RECONCILIATION_PROCESSOR_NOT_FOUND
            );
        }

        processor.process(run);
    }
}