package com.example.ledgercore.interest.command.service;

import com.example.ledgercore.interest.command.dto.ClaimedInterestRun;

public interface InterestRunDispatcher {

    void dispatch(ClaimedInterestRun run);
}