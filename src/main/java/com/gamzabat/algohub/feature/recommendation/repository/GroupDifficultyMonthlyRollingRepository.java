package com.gamzabat.algohub.feature.recommendation.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.gamzabat.algohub.feature.recommendation.domain.GroupDifficultyMonthlyRolling;

public interface GroupDifficultyMonthlyRollingRepository extends JpaRepository<GroupDifficultyMonthlyRolling, Long> {

    @Query(value = """
            SELECT *
            FROM group_difficulty_monthly_rolling g
            WHERE g.window_start = :windowStart AND g.window_end = :windowEnd
            ORDER BY ABS(g.avg_difficulty - :userAvg) ASC
            LIMIT 1
            """, nativeQuery = true)
    Optional<GroupDifficultyMonthlyRolling> findTopSimilarByWindow(@Param("windowStart") LocalDateTime windowStart,
                                                                   @Param("windowEnd") LocalDateTime windowEnd,
                                                                   @Param("userAvg") double userAvg);
}
