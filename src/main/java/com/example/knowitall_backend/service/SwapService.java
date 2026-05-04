package com.example.knowitall_backend.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.knowitall_backend.dto.SwapDTO;
import com.example.knowitall_backend.model.Swap;
import com.example.knowitall_backend.model.User;
import com.example.knowitall_backend.repository.SwapRepository;
import com.example.knowitall_backend.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SwapService {

    private final SwapRepository swapRepository;
    private final UserRepository userRepository;
    private final LedgerService ledgerService;

    /**
     * Initiates a new swap request.
     */
    @Transactional
    public SwapDTO initiateSwap(Swap swapRequest) {
        swapRequest.setSwapId(UUID.randomUUID().toString());
        swapRequest.setStatus(Swap.SwapStatus.REQUESTED);

        Swap savedSwap = swapRepository.save(swapRequest);
        return SwapDTO.fromEntity(savedSwap);
    }

    /**
     * Completes a swap, transfers tokens, and records it in the Trust Ledger.
     */
    @Transactional
    public SwapDTO completeSwap(String swapId, Integer rating, String feedback) {
        // findById returns an Optional; the orElseThrow ensures 'swap' is not
        // null[cite: 1]
        Swap swap = swapRepository.findById(swapId)
                .orElseThrow(() -> new RuntimeException("Swap not found"));

        if (swap.getStatus() != Swap.SwapStatus.ACTIVE) {
            throw new RuntimeException("Only active swaps can be completed");
        }

        // 1. Update User Balances
        User mentor = swap.getMentor();
        User learner = swap.getLearner();

        // Use primitive long for calculations to avoid Null Safety issues with Long
        // objects
        long amount = (swap.getTokenAmount() != null) ? swap.getTokenAmount() : 0L;
        long currentBalance = (learner.getSkillTokenBalance() != null) ? learner.getSkillTokenBalance() : 0L;

        if (currentBalance < amount) {
            throw new RuntimeException("Learner has insufficient tokens");
        }

        learner.setSkillTokenBalance(currentBalance - amount);
        mentor.setSkillTokenBalance(
                (mentor.getSkillTokenBalance() != null ? mentor.getSkillTokenBalance() : 0L) + amount);

        // 2. Update Swap Status
        swap.setStatus(Swap.SwapStatus.COMPLETED);

        // 3. Persist Changes
        userRepository.save(mentor);
        userRepository.save(learner);
        Swap completedSwap = swapRepository.save(swap);

        // 4. Record to Cryptographic Ledger
        ledgerService.recordSwapToLedger(completedSwap, rating, feedback);

        return SwapDTO.fromEntity(completedSwap);
    }

    public List<SwapDTO> getUserSwaps(String userId) {
        return swapRepository.findAllByUserId(userId)
                .stream()
                .map(SwapDTO::fromEntity)
                .toList();
    }
}