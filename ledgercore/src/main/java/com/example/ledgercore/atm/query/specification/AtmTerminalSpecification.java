package com.example.ledgercore.atm.query.specification;

import com.example.ledgercore.atm.entity.AtmTerminal;
import com.example.ledgercore.atm.enums.AtmTerminalStatus;
import org.springframework.data.jpa.domain.Specification;

public final class AtmTerminalSpecification {

    private AtmTerminalSpecification() {
    }

    public static Specification<AtmTerminal> search(
            String search
    ) {
        return (root, query, cb) -> {

            if (search == null || search.isBlank()) {
                return cb.conjunction();
            }

            String pattern =
                    "%" + search.trim().toLowerCase() + "%";

            return cb.or(
                    cb.like(
                            cb.lower(
                                    root.get("terminalCode")
                            ),
                            pattern
                    ),
                    cb.like(
                            cb.lower(
                                    root.get("location")
                            ),
                            pattern
                    )
            );
        };
    }

    public static Specification<AtmTerminal> hasStatus(
            AtmTerminalStatus status
    ) {
        return (root, query, cb) -> {

            if (status == null) {
                return cb.conjunction();
            }

            return cb.equal(
                    root.get("status"),
                    status
            );
        };
    }
}