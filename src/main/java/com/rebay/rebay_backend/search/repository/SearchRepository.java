package com.rebay.rebay_backend.search.repository;

import com.rebay.rebay_backend.search.entity.Search;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public interface SearchRepository extends JpaRepository<Search, Long> {

    // 하루(24시간) 내의 검색 기록을 기준으로 인기 검색어 상위 10개를 조회
    @Query(
            value = "SELECT keyword " +
                    "FROM searches " +
                    "WHERE created_at >= :oneDayAgo " +
                    "GROUP BY keyword " +
                    "ORDER BY COUNT(keyword) DESC " +
                    "LIMIT 10",
            nativeQuery = true
    )
    Set<String> findTop10PopularKeywordsInOneDay(@Param("oneDayAgo") LocalDateTime oneDayAgo);

    // 최근 7일간 검색한 키워드 목록 조회
    @Query(value = "SELECT keyword, created_at FROM searches " +
            "WHERE user_id = :userId AND created_at >= :oneWeekAgo",
            nativeQuery = true)
    List<Object[]> findRecentKeywordsWithTime(@Param("userId") Long userId,
                                              @Param("oneWeekAgo") LocalDateTime oneWeekAgo);

    List<Search> findByUserIdOrderByCreatedAt(Long userId);
}
