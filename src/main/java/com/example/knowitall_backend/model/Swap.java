package com.example.knowitall_backend.model;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "swaps")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Swap {

    @Id
    @Column(nullable = false, unique = true)
    private String swapId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentor_id", nullable = false)
    private User mentor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "learner_id", nullable = false)
    private User learner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentor_skill_id", nullable = false)
    private Skill mentorSkill;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "learner_skill_id")
    private Skill learnerSkill;

    @Enumerated(EnumType.STRING)
    @Column(name = "swap_type", nullable = false)
    private SwapType swapType;

    @Column(name = "token_amount", columnDefinition = "BIGINT DEFAULT 0")
    @Builder.Default
    private Long tokenAmount = 0L;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private SwapStatus status = SwapStatus.REQUESTED;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_method")
    @Builder.Default
    private VerificationMethod verificationMethod = VerificationMethod.NONE;

    @Column(name = "session_start_time")
    private Instant sessionStartTime;

    @Column(name = "session_end_time")
    private Instant sessionEndTime;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    public enum SwapStatus {
        REQUESTED, ACTIVE, COMPLETED, CANCELLED, DISPUTED
    }

    public enum SwapType {
        BARTER, TOKEN, HYBRID
    }

    public enum VerificationMethod {
        NONE, QR_HANDSHAKE, VIDEO_CALL, BOTH
    }
}