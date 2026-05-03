package com.example.knowitall_backend.dto;

import com.example.knowitall_backend.model.Skill;

public record SkillDTO(
        String skillId,
        String userId,
        String skillName,
        String description,
        String category,
        String proficiencyLevel,
        Integer tokenValue,
        Boolean verificationStatus,
        Integer endorsements,
        Long createdAt) {
    public static SkillDTO fromEntity(Skill skill) {
        return new SkillDTO(
                skill.getSkillId(),
                skill.getUser().getUid(),
                skill.getSkillName(),
                skill.getDescription(),
                skill.getCategory().name(),
                skill.getProficiencyLevel().name(),
                skill.getTokenValue(),
                skill.getVerificationStatus(),
                skill.getEndorsements(),
                skill.getCreatedAt() != null ? skill.getCreatedAt().toEpochMilli() : null);
    }
}