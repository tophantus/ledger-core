package com.example.ledgercore.interest.command.service;

import com.example.ledgercore.interest.command.dto.ClaimedInterestRun;
import com.example.ledgercore.interest.command.dto.InterestRunBatchResult;

public interface ProcessInterestRunBatchResultService {

    void process(
            ClaimedInterestRun run,
            InterestRunBatchResult result
    );
}