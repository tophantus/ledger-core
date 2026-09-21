package com.example.ledgercore.card.infrastructure.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class HmacCardPanHashService
        implements CardPanHashService {

    private static final String HMAC_ALGORITHM =
            "HmacSHA256";

    private final HexFormat hexFormat =
            HexFormat.of();

    @Value("${card.security.pan-hash-secret}")
    private String secret;

    @Override
    public String hash(String pan) {
        try {
            Mac mac =
                    Mac.getInstance(HMAC_ALGORITHM);

            SecretKeySpec key =
                    new SecretKeySpec(
                            secret.getBytes(
                                    StandardCharsets.UTF_8
                            ),
                            HMAC_ALGORITHM
                    );

            mac.init(key);

            byte[] hash =
                    mac.doFinal(
                            pan.getBytes(
                                    StandardCharsets.UTF_8
                            )
                    );

            return hexFormat.formatHex(hash);

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Failed to hash card PAN",
                    e
            );
        }
    }
}