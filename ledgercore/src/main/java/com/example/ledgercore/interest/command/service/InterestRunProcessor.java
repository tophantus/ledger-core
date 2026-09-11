package com.example.ledgercore.interest.command.service;

import com.example.ledgercore.interest.command.dto.ClaimedInterestRun;
import com.example.ledgercore.interest.enums.InterestRunType;

public interface InterestRunProcessor {

    InterestRunType getType();

    void process(ClaimedInterestRun run);
}