package com.example.knowitall_backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.knowitall_backend.model.TrustLedger;

@Repository
public interface LedgerRepository extends JpaRepository<TrustLedger, String> {

        // All ledger entries for a user (mentor or learner)
        @Query("SELECT t FROM TrustLedger t WHERE " +
                        "t.mentor.uid = :userId OR t.learner.uid = :userId " +
                        "ORDER BY t.createdAt DESC")
        List<TrustLedger> findAllByUserId(
                        @Param("userId") String userId,
                        Pageable pageable);

        // All entries for a specific swap
        List<TrustLedger> findBySwapSwapIdOrderByCreatedAtAsc(String swapId);

        // Latest entry — used for hash chain
        Optional<TrustLedger> findTopByOrderByCreatedAtDesc();

        // Average rating for a mentor
        @Query("SELECT AVG(t.ratingGiven) FROM TrustLedger t WHERE " +
                        "t.mentor.uid = :userId AND t.status = 'COMPLETED'")
        Double getAverageRatingForMentor(@Param("userId") String userId);

        // Completed swap count
        @Query("SELECT COUNT(t) FROM TrustLedger t WHERE " +
                        "(t.mentor.uid = :userId OR t.learner.uid = :userId) AND " +
                        "t.status = 'COMPLETED'")
        Long countCompletedSwapsByUserId(@Param("userId") String userId);

        // Disputed entries
        @Query("SELECT t FROM TrustLedger t WHERE " +
                        "(t.mentor.uid = :userId OR t.learner.uid = :userId) AND " +
                        "t.status = 'DISPUTED' " +
                        "ORDER BY t.createdAt DESC")
        List<TrustLedger> findDisputedByUserId(@Param("userId") String userId);
}