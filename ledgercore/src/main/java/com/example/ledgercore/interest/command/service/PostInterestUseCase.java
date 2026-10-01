package com.example.ledgercore.interest.command.service;

import com.example.ledgercore.interest.command.service.dto.PostInterestCommand;

public interface PostInterestUseCase {

    void execute(PostInterestCommand command);
}
