package com.example.ledgercore.interest.command.dto;

import java.util.UUID;

public interface InterestRunBatchResult {

    UUID lastProcessedId();

    long processedCount();

    boolean completed();
}