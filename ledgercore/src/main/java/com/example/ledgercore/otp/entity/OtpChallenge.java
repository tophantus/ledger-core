package com.example.ledgercore.otp.entity;

import com.example.ledgercore.otp.enums.OtpChannel;
import com.example.ledgercore.otp.enums.OtpPurpose;
import com.example.ledgercore.otp.enums.OtpStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "otp_challenges",
        indexes = {
                @Index(
                        name = "idx_otp_challenges_subject_id",
                        columnList = "subject_id"
                ),
                @Index(
                        name = "idx_otp_challenges_reference_id",
                        columnList = "reference_id"
                ),
                @Index(
                        name = "idx_otp_challenges_expires_at",
                        columnList = "expires_at"
                ),
                @Index(
                        name = "idx_otp_challenges_lookup",
                        columnList = "subject_id, reference_id, purpose, status"
                )
        }
)
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtpChallenge {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * User/account that owns this OTP.
     */
    @Column(
            name = "subject_id",
            nullable = false
    )
    private UUID subjectId;

    /**
     * Business resource associated with this OTP.
     * Example:
     * - null for SIGNUP
     * - transferId for CONFIRM_TRANSFER
     */
    @Column(name = "reference_id")
    private UUID referenceId;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "purpose",
            nullable = false,
            length = 50
    )
    private OtpPurpose purpose;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "channel",
            nullable = false,
            length = 20
    )
    private OtpChannel channel;

    @Column(
            name = "destination",
            nullable = false,
            length = 255
    )
    private String destination;

    /**
     * Never store OTP plaintext.
     */
    @Column(
            name = "otp_hash",
            nullable = false,
            length = 255
    )
    private String otpHash;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 20
    )
    private OtpStatus status;

    @Column(
            name = "attempts",
            nullable = false
    )
    private int attempts;

    @Column(
            name = "expires_at",
            nullable = false
    )
    private Instant expiresAt;

    @Column(name = "verified_at")
    private Instant verifiedAt;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    public boolean isExpired(Instant now) {
        return now.isAfter(expiresAt);
    }

    public boolean isVerified() {
        return status == OtpStatus.VERIFIED;
    }

    public void increaseAttempts() {
        attempts++;
    }

    public void verify(Instant now) {
        status = OtpStatus.VERIFIED;
        verifiedAt = now;
    }

    public void expire() {
        status = OtpStatus.EXPIRED;
    }

    public void lock() {
        status = OtpStatus.LOCKED;
    }

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }

        if (status == null) {
            status = OtpStatus.PENDING;
        }
    }
}