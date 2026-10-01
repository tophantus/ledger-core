package com.example.ledgercore.credit.command.service.impl;

import com.example.ledgercore.credit.command.service.OverdueCreditStatementDispatcher;
import com.example.ledgercore.credit.command.service.OverdueCreditStatementProcessor;
import com.example.ledgercore.credit.entity.CreditStatement;
import com.example.ledgercore.credit.enums.CreditStatementStatus;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class OverdueCreditStatementDispatcherImpl
        implements OverdueCreditStatementDispatcher {

    private final Map<
            CreditStatementStatus,
            OverdueCreditStatementProcessor
            > processors;

    public OverdueCreditStatementDispatcherImpl(
            List<OverdueCreditStatementProcessor> processors
    ) {
        Map<CreditStatementStatus, OverdueCreditStatementProcessor> map =
                new EnumMap<>(CreditStatementStatus.class);

        for (OverdueCreditStatementProcessor processor : processors) {

            OverdueCreditStatementProcessor previous =
                    map.put(
                            processor.getStatus(),
                            processor
                    );

            if (previous != null) {
                throw new IllegalStateException(
                        "Duplicate overdue credit statement processor for status: "
                                + processor.getStatus()
                );
            }
        }

        this.processors = Map.copyOf(map);
    }

    @Override
    public void dispatch(CreditStatement statement) {

        if (statement == null) {
            throw new IllegalArgumentException(
                    "statement must not be null"
            );
        }

        OverdueCreditStatementProcessor processor =
                processors.get(statement.getStatus());

        if (processor == null) {
            throw new IllegalStateException(
                    "No overdue credit statement processor found for status: "
                            + statement.getStatus()
            );
        }

        processor.process(statement);
    }
}