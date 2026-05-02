package com.example.knowitall_backend.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

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
import com.example.knowitall_backend.model.User;
import com.example.knowitall_backend.repository.SkillRepository;
import com.example.knowitall_backend.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/ledger")
@RequiredArgsConstructor
public class LedgerController {

    private final SkillRepository skillRepository;
    private final UserRepository userRepository;

    // POST /skills
    @PostMapping
    public ResponseEntity<?> addSkill(
            @RequestBody Map<String, Object> body,
            Authentication auth) {
        try {
            String userId = auth.getPrincipal().toString();
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Skill skill = Skill.builder()
                    .skillId(UUID.randomUUID().toString())
                    .user(user)
                    .skillName((String) body.get("skillName"))
                    .description((String) body.getOrDefault("description", ""))
                    .category(Skill.SkillCategory.valueOf(
                            ((String) body.getOrDefault("category", "DIGITAL")).toUpperCase()))
                    .proficiencyLevel(Skill.ProficiencyLevel.valueOf(
                            ((String) body.getOrDefault("proficiencyLevel", "BEGINNER")).toUpperCase()))
                    .tokenValue((Integer) body.getOrDefault("tokenValue", 10))
                    .verificationStatus(false)
                    .endorsements(0)
                    .build();

            skillRepository.save(skill);
            return ResponseEntity.ok(Map.of("success", true, "data", toDTO(skill)));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false, "error", e.getMessage()));
        }
    }

    // GET /skills/user/{userId}
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserSkills(@PathVariable String userId) {
        try {
            List<Map<String, Object>> skills = skillRepository
                    .findByUserUidOrderByCreatedAtDesc(userId)
                    .stream().map(this::toDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(Map.of("success", true, "data", skills));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false, "error", e.getMessage()));
        }
    }

    // GET /skills/search?query=&category=
    @GetMapping("/search")
    public ResponseEntity<?> searchSkills(
            @RequestParam String query,
            @RequestParam(required = false) String category) {
        try {
            List<Map<String, Object>> skills = skillRepository
                    .searchByNameAndCategory(query, category)
                    .stream().map(this::toDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(Map.of("success", true, "data", skills));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false, "error", e.getMessage()));
        }
    }

    // PUT /skills/{skillId}
    @PutMapping("/{skillId}")
    public ResponseEntity<?> updateSkill(
            @PathVariable String skillId,
            @RequestBody Map<String, Object> body,
            Authentication auth) {
        try {
            Skill skill = skillRepository.findById(skillId)
                    .orElseThrow(() -> new RuntimeException("Skill not found"));

            // Only the owner can update
            if (!skill.getUser().getUid().equals(auth.getPrincipal().toString())) {
                return ResponseEntity.status(403).body(Map.of(
                        "success", false, "error", "Forbidden"));
            }

            if (body.containsKey("skillName"))
                skill.setSkillName((String) body.get("skillName"));
            if (body.containsKey("description"))
                skill.setDescription((String) body.get("description"));
            if (body.containsKey("proficiencyLevel"))
                skill.setProficiencyLevel(Skill.ProficiencyLevel.valueOf(
                        ((String) body.get("proficiencyLevel")).toUpperCase()));
            if (body.containsKey("tokenValue"))
                skill.setTokenValue((Integer) body.get("tokenValue"));

            skillRepository.save(skill);
            return ResponseEntity.ok(Map.of("success", true, "data", toDTO(skill)));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false, "error", e.getMessage()));
        }
    }

    // DELETE /skills/{skillId}
    @DeleteMapping("/{skillId}")
    public ResponseEntity<?> deleteSkill(
            @PathVariable String skillId,
            Authentication auth) {
        try {
            Skill skill = skillRepository.findById(skillId)
                    .orElseThrow(() -> new RuntimeException("Skill not found"));

            if (!skill.getUser().getUid().equals(auth.getPrincipal().toString())) {
                return ResponseEntity.status(403).body(Map.of(
                        "success", false, "error", "Forbidden"));
            }

            skillRepository.delete(skill);
            return ResponseEntity.ok(Map.of("success", true, "message", "Skill deleted"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false, "error", e.getMessage()));
        }
    }

    // POST /skills/{skillId}/endorse
    @PostMapping("/{skillId}/endorse")
    public ResponseEntity<?> endorseSkill(
            @PathVariable String skillId,
            Authentication auth) {
        try {
            Skill skill = skillRepository.findById(skillId)
                    .orElseThrow(() -> new RuntimeException("Skill not found"));

            // Can't endorse your own skill
            if (skill.getUser().getUid().equals(auth.getPrincipal().toString())) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false, "error", "Cannot endorse your own skill"));
            }

            skill.setEndorsements(skill.getEndorsements() + 1);
            skillRepository.save(skill);
            return ResponseEntity.ok(Map.of("success", true, "message", "Skill endorsed"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false, "error", e.getMessage()));
        }
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private Map<String, Object> toDTO(Skill skill) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("skillId", skill.getSkillId());
        dto.put("userId", skill.getUser().getUid());
        dto.put("skillName", skill.getSkillName());
        dto.put("description", skill.getDescription());
        dto.put("category", skill.getCategory().name());
        dto.put("proficiencyLevel", skill.getProficiencyLevel().name());
        dto.put("tokenValue", skill.getTokenValue());
        dto.put("verificationStatus", skill.getVerificationStatus());
        dto.put("endorsements", skill.getEndorsements());
        dto.put("createdAt", skill.getCreatedAt() != null ? skill.getCreatedAt().toEpochMilli() : null);
        return dto;
    }
}