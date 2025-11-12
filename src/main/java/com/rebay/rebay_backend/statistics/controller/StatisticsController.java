package com.rebay.rebay_backend.statistics.controller;

import com.rebay.rebay_backend.Post.service.PostService;
import com.rebay.rebay_backend.review.service.ReviewService;
import com.rebay.rebay_backend.social.service.FollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/statistics")
public class StatisticsController {
    private final ReviewService reviewService;
    private final PostService postService;
    private final FollowService followService;

    @GetMapping("/userProfile/{userId}")
    public ResponseEntity<Map<String,Long>> getStatisticsByUserProfile(@PathVariable Long userId) {
        Map<String,Long> counts = new HashMap<>();
        counts.put("post", postService.getPostsCountByUser(userId));
        counts.put("review", reviewService.getReviewsCountByUser(userId));
        counts.put("follower", followService.getFollowersCount(userId));
        counts.put("following", followService.getFollowingCount(userId));
        return ResponseEntity.ok(counts);
    }
}
