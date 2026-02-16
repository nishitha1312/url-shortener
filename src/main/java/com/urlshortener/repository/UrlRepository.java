package com.urlshortener.repository;

import com.urlshortener.model.Url;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UrlRepository extends JpaRepository<Url, Long> {

    Optional<Url> findByShortCode(String shortCode);

    Optional<Url> findByCustomAlias(String customAlias);

    boolean existsByShortCode(String shortCode);

    boolean existsByCustomAlias(String customAlias);

    @Query("SELECT u FROM Url u WHERE u.active = true AND (u.expiresAt IS NULL OR u.expiresAt > :now)")
    List<Url> findAllActive(@Param("now") LocalDateTime now);

    @Query("SELECT u FROM Url u WHERE u.expiresAt IS NOT NULL AND u.expiresAt < :now AND u.active = true")
    List<Url> findExpired(@Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE Url u SET u.active = false WHERE u.expiresAt IS NOT NULL AND u.expiresAt < :now")
    int deactivateExpiredUrls(@Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE Url u SET u.clickCount = u.clickCount + 1 WHERE u.shortCode = :shortCode")
    void incrementClickCount(@Param("shortCode") String shortCode);

    List<Url> findByCreatedBy(String createdBy);

    @Query("SELECT COUNT(u) FROM Url u WHERE u.active = true")
    long countActiveUrls();
}
