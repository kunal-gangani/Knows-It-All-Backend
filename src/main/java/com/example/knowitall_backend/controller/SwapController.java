package com.example.knowitall_backend.controller;

import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.knowitall_backend.model.Skill;
import com.example.knowitall_backend.model.Swap;
import com.example.knowitall_backend.model.TrustLedger;
import com.example.knowitall_backend.model.User;
import com.example.knowitall_backend.repository.LedgerRepository;
import com.example.knowitall_backend.repository.SkillRepository;
import com.example.knowitall_backend.repository.SwapRepository;
import com.example.knowitall_backend.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/swap")
@RequiredArgsConstructor
public class SwapController {

    private final SwapRepository swapRepository;
    private final UserRepository userRepository;
    private final SkillRepository skillRepository;
    private final LedgerRepository ledgerRepository;

    // POST /swap/request
    @PostMapping("/request")
    public ResponseEntity<?> requestSwap(
            @RequestBody Map<String, Object> body,
            Authentication auth) {
        try {
            String learnerId = auth.getPrincipal().toString();
            String mentorId = (String) body.get("mentorId");
            String mentorSkillId = (String) body.get("mentorSkillId");
            String swapTypeStr = (String) body.getOrDefault("swapType", "TOKEN");
            Long tokenAmount = body.get("tokenAmount") != null ? Long.valueOf(body.get("tokenAmount").toString()) : 0L;

            User mentor = userRepository.findById(mentorId)
                    .orElseThrow(() -> new RuntimeException("Mentor not found"));
            User learner = userRepository.findById(learnerId)
                    .orElseThrow(() -> new RuntimeException("Learner not found"));
            Skill mentorSkill = skillRepository.findById(mentorSkillId)
                    .orElseThrow(() -> new RuntimeException("Skill not found"));

            // Optional learner skill for BARTER swaps
            Skill learnerSkill = null;
            if (body.get("learnerSkillId") != null) {
                learnerSkill = skillRepository.findById(
                        (String) body.get("learnerSkillId")).orElse(null);
            }

            Swap swap = Swap.builder()
                    .swapId(UUID.randomUUID().toString())
                    .mentor(mentor)
                    .learner(learner)
                    .mentorSkill(mentorSkill)
                    .learnerSkill(learnerSkill)
                    .swapType(Swap.SwapType.valueOf(swapTypeStr.toUpperCase()))
                    .tokenAmount(tokenAmount)
                    .status(Swap.SwapStatus.REQUESTED)
                    .verificationMethod(Swap.VerificationMethod.NONE)
                    .build();

            swapRepository.save(swap);
            return ResponseEntity.ok(Map.of("success", true, "data", toDTO(swap)));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false, "error", e.getMessage()));
        }
    }

    // GET /swap/{swapId}
    @GetMapping("/{swapId}")
    public ResponseEntity<?> getSwapDetails(@PathVariable String swapId) {
        try {
            Swap swap = swapRepository.findById(swapId)
                    .orElseThrow(() -> new RuntimeException("Swap not found"));
            return ResponseEntity.ok(Map.of("success", true, "data", toDTO(swap)));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false, "error", e.getMessage()));
        }
    }

    // PUT /swap/{swapId}/accept
    @PutMapping("/{swapId}/accept")
    public ResponseEntity<?> acceptSwap(
            @PathVariable String swapId,
            Authentication auth) {
        try {
            Swap swap = swapRepository.findById(swapId)
                    .orElseThrow(() -> new RuntimeException("Swap not found"));

            // Only the mentor can accept
            if (!swap.getMentor().getUid().equals(auth.getPrincipal().toString())) {
                return ResponseEntity.status(403).body(Map.of(
                        "success", false, "error", "Only the mentor can accept a swap"));
            }

            swap.setStatus(Swap.SwapStatus.ACTIVE);
            swapRepository.save(swap);
            return ResponseEntity.ok(Map.of("success", true, "data", toDTO(swap)));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false, "error", e.getMessage()));
        }
    }

    // PUT /swap/{swapId}/complete
    @PutMapping("/{swapId}/complete")
    @Transactional
    public ResponseEntity<?> completeSwap(
            @PathVariable String swapId,
            Authentication auth) {
        try {
            Swap swap = swapRepository.findById(swapId)
                    .orElseThrow(() -> new RuntimeException("Swap not found"));

            swap.setStatus(Swap.SwapStatus.COMPLETED);
            swapRepository.save(swap);

            // Transfer tokens from learner to mentor
            if (swap.getTokenAmount() > 0) {
                User learner = swap.getLearner();
                User mentor = swap.getMentor();
                if (learner.getSkillTokenBalance() >= swap.getTokenAmount()) {
                    learner.setSkillTokenBalance(
                            learner.getSkillTokenBalance() - swap.getTokenAmount());
                    mentor.setSkillTokenBalance(
                            mentor.getSkillTokenBalance() + swap.getTokenAmount());
                    userRepository.save(learner);
                    userRepository.save(mentor);
                }
            }

            return ResponseEntity.ok(Map.of("success", true, "data", toDTO(swap)));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false, "error", e.getMessage()));
        }
    }

    // DELETE /swap/{swapId}
    @DeleteMapping("/{swapId}")
    public ResponseEntity<?> cancelSwap(
            @PathVariable String swapId,
            Authentication auth) {
        try {
            Swap swap = swapRepository.findById(swapId)
                    .orElseThrow(() -> new RuntimeException("Swap not found"));

            String userId = auth.getPrincipal().toString();
            boolean isMentor = swap.getMentor().getUid().equals(userId);
            boolean isLearner = swap.getLearner().getUid().equals(userId);

            if (!isMentor && !isLearner) {
                return ResponseEntity.status(403).body(Map.of(
                        "success", false, "error", "Forbidden"));
            }

            swap.setStatus(Swap.SwapStatus.CANCELLED);
            swapRepository.save(swap);
            return ResponseEntity.ok(Map.of("success", true,
                    "message", "Swap cancelled"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false, "error", e.getMessage()));
        }
    }

    // POST /swap/{swapId}/rating
    @PostMapping("/{swapId}/rating")
    public ResponseEntity<?> rateSwap(
            @PathVariable String swapId,
            @RequestBody Map<String, Object> body,
            Authentication auth) {
        try {
            Swap swap = swapRepository.findById(swapId)
                    .orElseThrow(() -> new RuntimeException("Swap not found"));

            if (swap.getStatus() != Swap.SwapStatus.COMPLETED) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false, "error", "Can only rate completed swaps"));
            }

            int rating = (Integer) body.getOrDefault("rating", 5);
            String comment = (String) body.getOrDefault("comment", "");

            // Get previous hash for chain integrity
            String previousHash = ledgerRepository.findTopByOrderByCreatedAtDesc()
                    .map(TrustLedger::getCurrentHash)
                    .orElse("");

            // Build transaction data for hashing
            String transactionData = swapId + swap.getMentor().getUid()
                    + swap.getLearner().getUid()
                    + swap.getMentorSkill().getSkillName()
                    + rating
                    + System.currentTimeMillis();

            String currentHash = sha256(transactionData + previousHash);

            TrustLedger entry = TrustLedger.builder()
                    .transactionId(UUID.randomUUID().toString())
                    .swap(swap)
                    .mentor(swap.getMentor())
                    .learner(swap.getLearner())
                    .skillName(swap.getMentorSkill().getSkillName())
                    .previousHash(previousHash)
                    .currentHash(currentHash)
                    .ratingGiven(rating)
                    .ratingComment(comment)
                    .status(TrustLedger.LedgerStatus.COMPLETED)
                    .build();

            ledgerRepository.save(entry);

            // Update mentor trust score
            User mentor = swap.getMentor();
            Double avgRating = ledgerRepository.getAverageRatingForMentor(
                    mentor.getUid());
            if (avgRating != null) {
                mentor.setTrustScore(avgRating.floatValue());
                userRepository.save(mentor);
            }

            return ResponseEntity.ok(Map.of("success", true,
                    "message", "Rating submitted"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false, "error", e.getMessage()));
        }
    }

    // GET /swap/user/active
    @GetMapping("/user/active")
    public ResponseEntity<?> getActiveSwaps(Authentication auth) {
        try {
            String userId = auth.getPrincipal().toString();
            List<Map<String, Object>> swaps = swapRepository
                    .findActiveSwapsByUserId(userId)
                    .stream().map(this::toDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(Map.of("success", true, "data", swaps));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false, "error", e.getMessage()));
        }
    }

    // GET /swap/user/history?limit=10&offset=0
    @GetMapping("/user/history")
    public ResponseEntity<?> getSwapHistory(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "0") int offset,
            Authentication auth) {
        try {
            String userId = auth.getPrincipal().toString();
            List<Map<String, Object>> swaps = swapRepository
                    .findCompletedSwapsByUserId(userId,
                            PageRequest.of(offset / limit, limit))
                    .stream().map(this::toDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(Map.of("success", true, "data", swaps));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false, "error", e.getMessage()));
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Map<String, Object> toDTO(Swap swap) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("swapId", swap.getSwapId());
        dto.put("mentorId", swap.getMentor().getUid());
        dto.put("learnerId", swap.getLearner().getUid());
        dto.put("mentorName", swap.getMentor().getName());
        dto.put("learnerName", swap.getLearner().getName());
        dto.put("skillName", swap.getMentorSkill().getSkillName());
        dto.put("mentorSkillId", swap.getMentorSkill().getSkillId());
        dto.put("learnerSkillId", swap.getLearnerSkill() != null ? swap.getLearnerSkill().getSkillId() : null);
        dto.put("swapType", swap.getSwapType().name());
        dto.put("tokenAmount", swap.getTokenAmount());
        dto.put("status", swap.getStatus().name());
        dto.put("verificationMethod", swap.getVerificationMethod().name());
        dto.put("sessionStartTime",
                swap.getSessionStartTime() != null ? swap.getSessionStartTime().toEpochMilli() : null);
        dto.put("sessionEndTime", swap.getSessionEndTime() != null ? swap.getSessionEndTime().toEpochMilli() : null);
        dto.put("createdAt", swap.getCreatedAt() != null ? swap.getCreatedAt().toEpochMilli() : null);
        return dto;
    }

    private String sha256(String input) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes());
            StringBuilder hex = new StringBuilder();
            for (byte b : hash)
                hex.append(String.format("%02x", b));
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            return UUID.randomUUID().toString();
        }
    }
}