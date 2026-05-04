package com.example.knowitall_backend.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.knowitall_backend.model.Swap;
import com.example.knowitall_backend.model.TrustLedger;
import com.example.knowitall_backend.repository.LedgerRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LedgerService {

    private final LedgerRepository ledgerRepository;

    @Transactional
    public void recordSwapToLedger(Swap swap, Integer rating, String feedback) {
        // 1. Fetch previous hash[cite: 2]
        // findTopByOrderByCreatedAtDesc helps maintain the chain integrity[cite: 2]
        String previousHash = ledgerRepository.findTopByOrderByCreatedAtDesc()
                .map(TrustLedger::getCurrentHash)
                .orElse("0");

        // 2. Generate current hash
        // The hash links this transaction to the previous one, creating the "chain"
        String dataToHash = swap.getSwapId() + swap.getStatus() + rating + feedback + previousHash;
        String currentHash = calculateSHA256(dataToHash);

        // 3. Build the Ledger Entry
        // Note: transactionId matches the @Id in your TrustLedger entity
        TrustLedger entry = TrustLedger.builder()
                .transactionId(UUID.randomUUID().toString())
                .swap(swap)
                .mentor(swap.getMentor())
                .learner(swap.getLearner())
                .skillName(swap.getMentorSkill() != null ? swap.getMentorSkill().getSkillName() : "Unknown Skill")
                .previousHash(previousHash)
                .currentHash(currentHash)
                .ratingGiven(rating)
                .ratingComment(feedback)
                .status(TrustLedger.LedgerStatus.COMPLETED)
                .build();

        // The warning here is likely because JpaRepository.save() is marked @NonNull
        // @SuppressWarnings("null") can be used if the IDE continues to flag it
        ledgerRepository.save(entry);
    }

    private String calculateSHA256(String data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Hash algorithm not found", e);
        }
    }
}