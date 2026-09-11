package com.example.ledgercore.common.currency;

import java.math.RoundingMode;

public enum Currency {

    VND(
            0,
            RoundingMode.HALF_UP
    ),

    USD(
            2,
            RoundingMode.HALF_UP
    );

    private final int scale;
    private final RoundingMode roundingMode;

    Currency(
            int scale,
            RoundingMode roundingMode
    ) {
        this.scale = scale;
        this.roundingMode = roundingMode;
    }

    public int scale() {
        return scale;
    }

    public RoundingMode roundingMode() {
        return roundingMode;
    }

    public static Currency fromCode(
            String code
    ) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException(
                    "Currency code must not be blank"
            );
        }

        try {
            return valueOf(
                    code.trim().toUpperCase()
            );
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Unsupported currency: " + code,
                    exception
            );
        }
    }
}