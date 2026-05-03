package com.example.knowitall_backend.controller;

import com.example.knowitall_backend.dto.SkillDTO;
import com.example.knowitall_backend.model.Skill;
import com.example.knowitall_backend.model.User;
import com.example.knowitall_backend.repository.SkillRepository;
import com.example.knowitall_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/skills")
@RequiredArgsConstructor
public class SkillController {

    private final SkillRepository skillRepository;
    private final UserRepository userRepository;

    // POST: Add a new skill
    @PostMapping
    public ResponseEntity<?> addSkill(@RequestBody Map<String, Object> body, Authentication auth) {
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
            return ResponseEntity.ok(Map.of("success", true, "data", SkillDTO.fromEntity(skill)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    // GET: Fetch all skills for a specific user
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserSkills(@PathVariable String userId) {
        try {
            List<SkillDTO> skills = skillRepository
                    .findByUserUidOrderByCreatedAtDesc(userId)
                    .stream()
                    .map(SkillDTO::fromEntity)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(Map.of("success", true, "data", skills));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    // GET: Search skills by query and optional category
    @GetMapping("/search")
    public ResponseEntity<?> searchSkills(
            @RequestParam String query,
            @RequestParam(required = false) String category) {
        try {
            List<SkillDTO> skills = skillRepository
                    .searchByNameAndCategory(query, category)
                    .stream()
                    .map(SkillDTO::fromEntity)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(Map.of("success", true, "data", skills));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    // PUT: Update an existing skill (Owner only)
    @PutMapping("/{skillId}")
    public ResponseEntity<?> updateSkill(
            @PathVariable String skillId,
            @RequestBody Map<String, Object> body,
            Authentication auth) {
        try {
            Skill skill = skillRepository.findById(skillId)
                    .orElseThrow(() -> new RuntimeException("Skill not found"));

            if (!skill.getUser().getUid().equals(auth.getPrincipal().toString())) {
                return ResponseEntity.status(403).body(Map.of("success", false, "error", "Unauthorized"));
            }

            if (body.containsKey("skillName"))
                skill.setSkillName((String) body.get("skillName"));
            if (body.containsKey("description"))
                skill.setDescription((String) body.get("description"));
            if (body.containsKey("proficiencyLevel")) {
                skill.setProficiencyLevel(Skill.ProficiencyLevel.valueOf(
                        ((String) body.get("proficiencyLevel")).toUpperCase()));
            }
            if (body.containsKey("tokenValue"))
                skill.setTokenValue((Integer) body.get("tokenValue"));

            skillRepository.save(skill);
            return ResponseEntity.ok(Map.of("success", true, "data", SkillDTO.fromEntity(skill)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    // DELETE: Remove a skill (Owner only)
    @DeleteMapping("/{skillId}")
    public ResponseEntity<?> deleteSkill(@PathVariable String skillId, Authentication auth) {
        try {
            Skill skill = skillRepository.findById(skillId)
                    .orElseThrow(() -> new RuntimeException("Skill not found"));

            if (!skill.getUser().getUid().equals(auth.getPrincipal().toString())) {
                return ResponseEntity.status(403).body(Map.of("success", false, "error", "Unauthorized"));
            }

            skillRepository.delete(skill);
            return ResponseEntity.ok(Map.of("success", true, "message", "Skill deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    // POST: Endorse a skill (Cannot endorse own)
    @PostMapping("/{skillId}/endorse")
    public ResponseEntity<?> endorseSkill(@PathVariable String skillId, Authentication auth) {
        try {
            Skill skill = skillRepository.findById(skillId)
                    .orElseThrow(() -> new RuntimeException("Skill not found"));

            if (skill.getUser().getUid().equals(auth.getPrincipal().toString())) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "error", "Self-endorsement not allowed"));
            }

            skill.setEndorsements(skill.getEndorsements() + 1);
            skillRepository.save(skill);
            return ResponseEntity.ok(Map.of("success", true, "message", "Endorsement added"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }
}