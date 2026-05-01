package com.example.knowitall_backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.knowitall_backend.model.Skill;

@Repository
public interface SkillRepository extends JpaRepository<Skill, String> {

        List<Skill> findByUserUid(String userId);

        List<Skill> findByUserUidOrderByCreatedAtDesc(String userId);

        // Search by skill name (case-insensitive)
        @Query("SELECT s FROM Skill s WHERE LOWER(s.skillName) LIKE LOWER(CONCAT('%', :query, '%'))")
        List<Skill> searchByName(@Param("query") String query);

        // Search by name and category
        @Query("SELECT s FROM Skill s WHERE " +
                        "LOWER(s.skillName) LIKE LOWER(CONCAT('%', :query, '%')) AND " +
                        "(:category IS NULL OR s.category = :category)")
        List<Skill> searchByNameAndCategory(
                        @Param("query") String query,
                        @Param("category") String category);

        // Get verified skills only
        List<Skill> findByVerificationStatusTrue();

        // Count endorsements for a skill
        @Query("SELECT s.endorsements FROM Skill s WHERE s.skillId = :skillId")
        Integer getEndorsementCount(@Param("skillId") String skillId);
}