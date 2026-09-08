package com.example.ledgercore.atm.query.dto;

import com.example.ledgercore.atm.enums.AtmTerminalStatus;

public record AtmTerminalQuery(
        int page,
        int size,
        String search,
        AtmTerminalStatus status
) {

    public AtmTerminalQuery {
        if (page < 0) {
            page = 0;
        }

        if (size <= 0) {
            size = 20;
        }

        if (size > 100) {
            size = 100;
        }

        if (search != null) {
            search = search.trim();
        }
    }
}