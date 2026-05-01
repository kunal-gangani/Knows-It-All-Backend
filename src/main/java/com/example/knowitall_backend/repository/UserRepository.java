package com.example.knowitall_backend.repository;

import com.example.knowitall_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    // Find users within a bounding box for the Radar screen
    @Query("SELECT u FROM User u WHERE " +
            "u.latitude BETWEEN :latMin AND :latMax AND " +
            "u.longitude BETWEEN :lonMin AND :lonMax AND " +
            "u.uid != :excludeUserId")
    List<User> findUsersInBoundingBox(
            @Param("latMin") Double latMin,
            @Param("latMax") Double latMax,
            @Param("lonMin") Double lonMin,
            @Param("lonMax") Double lonMax,
            @Param("excludeUserId") String excludeUserId);

    // Online users only — for the green dot filter
    @Query("SELECT u FROM User u WHERE " +
            "u.latitude BETWEEN :latMin AND :latMax AND " +
            "u.longitude BETWEEN :lonMin AND :lonMax AND " +
            "u.isOnline = true AND " +
            "u.uid != :excludeUserId")
    List<User> findOnlineUsersInBoundingBox(
            @Param("latMin") Double latMin,
            @Param("latMax") Double latMax,
            @Param("lonMin") Double lonMin,
            @Param("lonMax") Double lonMax,
            @Param("excludeUserId") String excludeUserId);
}