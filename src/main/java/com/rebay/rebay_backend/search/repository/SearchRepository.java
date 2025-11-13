package com.rebay.rebay_backend.search.repository;

import com.rebay.rebay_backend.search.entity.Search;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Set;

public interface SearchRepository extends JpaRepository<Search, Long> {

    /**
     * 하루(24시간) 내의 검색 기록을 기준으로 인기 검색어 상위 10개를 조회합니다.
     *
     * @param oneDayAgo 현재 시간으로부터 하루(24시간) 전 시간
     * @return 검색어(keyword) 문자열의 상위 10개 Set
     */
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
}
