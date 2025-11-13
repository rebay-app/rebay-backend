package com.rebay.rebay_backend.statistics.controller;

import com.rebay.rebay_backend.Post.dto.PostResponse;
import com.rebay.rebay_backend.Post.service.PostService;
import com.rebay.rebay_backend.review.service.ReviewService;
import com.rebay.rebay_backend.social.service.FollowService;
import com.rebay.rebay_backend.statistics.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/statistics")
public class StatisticsController {
    private final ReviewService reviewService;
    private final PostService postService;
    private final FollowService followService;
    private final StatisticsService statisticsService;

    @GetMapping("/userProfile/{userId}")
    public ResponseEntity<Map<String,Long>> getStatisticsByUserProfile(@PathVariable Long userId) {
        Map<String,Long> counts = new HashMap<>();
        counts.put("post", postService.getPostsCountByUser(userId));
        counts.put("review", reviewService.getReviewsCountByUser(userId));
        counts.put("follower", followService.getFollowersCount(userId));
        counts.put("following", followService.getFollowingCount(userId));
        return ResponseEntity.ok(counts);
    }

    // 좋아요 기준 일주일 간 인기상품
    @GetMapping("/popular")
    public ResponseEntity<Page<PostResponse>> getTopLikedProductsLastWeek(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(statisticsService.getTopLikedProductsLastWeek(pageable));
    }

    // 요새 많이 팔려요 = 일주일 간 잘 팔린 품목

    // 인기 검색어 = 검색 기록 많은 거
    @GetMapping("/top10Keyword")
    public ResponseEntity<Set<String>> getDailyTop10Keywords() {
        return ResponseEntity.ok(statisticsService.getDailyTop10Keywords());
    }

    // 시세

    // 얼마 벌었어요 ? 아니면 사용자 평균 판매수익 보여주는것도 좋을듯

    // 사용자 검색어 기록 + 좋아요 기록 기반 게시글 추천 알고리즘
}
