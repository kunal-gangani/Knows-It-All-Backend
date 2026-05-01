package com.example.knowitall_backend.repository;

import com.example.knowitall_backend.model.Swap;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SwapRepository extends JpaRepository<Swap, String> {

        // All swaps for a user (as mentor OR learner)
        @Query("SELECT s FROM Swap s WHERE " +
                        "s.mentor.uid = :userId OR s.learner.uid = :userId " +
                        "ORDER BY s.updatedAt DESC")
        List<Swap> findAllByUserId(@Param("userId") String userId);

        // Active swaps (REQUESTED or ACTIVE)
        @Query("SELECT s FROM Swap s WHERE " +
                        "(s.mentor.uid = :userId OR s.learner.uid = :userId) AND " +
                        "s.status IN ('REQUESTED', 'ACTIVE') " +
                        "ORDER BY s.createdAt DESC")
        List<Swap> findActiveSwapsByUserId(@Param("userId") String userId);

        // Completed swap history with pagination
        @Query("SELECT s FROM Swap s WHERE " +
                        "(s.mentor.uid = :userId OR s.learner.uid = :userId) AND " +
                        "s.status = 'COMPLETED' " +
                        "ORDER BY s.updatedAt DESC")
        List<Swap> findCompletedSwapsByUserId(
                        @Param("userId") String userId,
                        Pageable pageable);

        // Count pending requests for a user
        @Query("SELECT COUNT(s) FROM Swap s WHERE " +
                        "s.learner.uid = :userId AND s.status = 'REQUESTED'")
        Long countPendingRequestsByLearnerId(@Param("userId") String userId);
}