package com.example.ledgercore.interest.command.handler;

import com.example.ledgercore.interest.command.dto.ClaimedInterestRun;
import com.example.ledgercore.interest.command.port.inbound.DispatchInterestRunUseCase;
import com.example.ledgercore.interest.command.service.InterestRunDispatcher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DispatchInterestRunHandler
        implements DispatchInterestRunUseCase {

    private final InterestRunDispatcher dispatcher;

    @Override
    public void execute(ClaimedInterestRun run) {
        dispatcher.dispatch(run);
    }
}