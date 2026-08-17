package com.example.ledgercore.role.entity;

import com.example.ledgercore.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(
        name = "user_roles",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_roles_user_role",
                        columnNames = {"user_id", "role_id"}
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRole {

    @EmbeddedId
    private UserRoleId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(
            name = "user_id",
            nullable = false
    )
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("roleId")
    @JoinColumn(
            name = "role_id",
            nullable = false
    )
    private Role role;

    @Column(
            name = "assigned_at",
            nullable = false,
            updatable = false
    )
    private Instant assignedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "assigned_by",
            foreignKey = @ForeignKey(
                    name = "fk_user_roles_assigned_by"
            )
    )
    private User assignedBy;

    @PrePersist
    protected void prePersist() {
        if (assignedAt == null) {
            assignedAt = Instant.now();
        }
    }
}