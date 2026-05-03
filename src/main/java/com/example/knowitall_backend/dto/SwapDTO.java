package com.example.knowitall_backend.dto;

import com.example.knowitall_backend.model.Swap;

public record SwapDTO(
        String swapId,
        String mentorId,
        String learnerId,
        String mentorName,
        String learnerName,
        String skillName,
        String mentorSkillId,
        String learnerSkillId,
        String swapType,
        Long tokenAmount,
        String status,
        Long createdAt) {
    public static SwapDTO fromEntity(Swap swap) {
        return new SwapDTO(
                swap.getSwapId(),
                swap.getMentor().getUid(),
                swap.getLearner().getUid(),
                swap.getMentor().getName(),
                swap.getLearner().getName(),
                swap.getMentorSkill().getSkillName(),
                swap.getMentorSkill().getSkillId(),
                swap.getLearnerSkill() != null ? swap.getLearnerSkill().getSkillId() : null,
                swap.getSwapType().name(),
                swap.getTokenAmount(),
                swap.getStatus().name(),
                swap.getCreatedAt() != null ? swap.getCreatedAt().toEpochMilli() : null);
    }
}