package com.urlshortener.repository;

import com.urlshortener.model.ClickEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ClickEventRepository extends JpaRepository<ClickEvent, Long> {

    List<ClickEvent> findByShortCode(String shortCode);

    long countByShortCode(String shortCode);

    long countByShortCodeAndClickedAtAfter(String shortCode, LocalDateTime after);

    @Query("SELECT COUNT(c) FROM ClickEvent c WHERE c.shortCode = :shortCode " +
           "AND c.clickedAt BETWEEN :start AND :end")
    long countByShortCodeAndDateRange(
        @Param("shortCode") String shortCode,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );

    @Query("SELECT c.country, COUNT(c) FROM ClickEvent c WHERE c.shortCode = :shortCode " +
           "GROUP BY c.country ORDER BY COUNT(c) DESC")
    List<Object[]> countByCountry(@Param("shortCode") String shortCode);

    @Query("SELECT CAST(c.clickedAt AS date), COUNT(c) FROM ClickEvent c " +
           "WHERE c.shortCode = :shortCode AND c.clickedAt >= :since " +
           "GROUP BY CAST(c.clickedAt AS date) ORDER BY CAST(c.clickedAt AS date) DESC")
    List<Object[]> getDailyClickStats(
        @Param("shortCode") String shortCode,
        @Param("since") LocalDateTime since
    );
}
