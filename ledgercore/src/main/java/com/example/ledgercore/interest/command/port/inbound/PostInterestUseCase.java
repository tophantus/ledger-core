package com.example.ledgercore.interest.command.port.inbound;

import com.example.ledgercore.interest.command.dto.PostInterestCommand;

public interface PostInterestUseCase {

    void execute(PostInterestCommand command);
}
