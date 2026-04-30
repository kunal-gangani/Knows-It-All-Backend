package com.example.knowitall_backend.model;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;

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
@Table(name = "trust_ledger")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrustLedger {

    @Id
    @Column(nullable = false, unique = true)
    private String transactionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "swap_id", nullable = false)
    private Swap swap;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentor_id", nullable = false)
    private User mentor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "learner_id", nullable = false)
    private User learner;

    @Column(name = "skill_name")
    private String skillName;

    @Column(name = "previous_hash")
    private String previousHash;

    @Column(name = "current_hash")
    private String currentHash;

    @Column(name = "rating_given")
    private Integer ratingGiven;

    @Column(name = "rating_comment", columnDefinition = "TEXT")
    private String ratingComment;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private LedgerStatus status = LedgerStatus.COMPLETED;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    public enum LedgerStatus {
        COMPLETED, DISPUTED, RESOLVED
    }
}