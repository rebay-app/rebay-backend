package com.rebay.rebay_backend.statistics.service;

import com.rebay.rebay_backend.Post.dto.HashtagResponse;
import com.rebay.rebay_backend.Post.dto.PostResponse;
import com.rebay.rebay_backend.Post.entity.Post;
import com.rebay.rebay_backend.Post.entity.ProductCategory;
import com.rebay.rebay_backend.Post.repository.PostRepository;
import com.rebay.rebay_backend.payment.repository.PaymentRepository;
import com.rebay.rebay_backend.search.repository.SearchRepository;
import com.rebay.rebay_backend.social.repository.LikeRepository;
import com.rebay.rebay_backend.statistics.dto.RecommendedPostDto;
import com.rebay.rebay_backend.user.dto.UserResponse;
import com.rebay.rebay_backend.user.entity.User;
import com.rebay.rebay_backend.user.repository.UserRepository;
import com.rebay.rebay_backend.user.service.AuthenticationService;
import com.rebay.rebay_backend.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticsService {
    private static final double W_LIKE = 0.7;    // 좋아요 기반 점수 가중치
    private static final double W_SEARCH = 0.3;  // 검색 기반 점수 가중치
    private static final long TIME_PERIOD_DAYS = 30; // 검색 기록 분석 기간 (30일)

    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final SearchRepository searchRepository;
    private final LikeRepository likeRepository;
    private final PostRepository postRepository;
    private final UserService userService;
    private final AuthenticationService authenticationService;

    @CachePut(value = "weeklyTopPosts", key = "'latest'")
    public List<PostResponse> getTopLikedProductsLastWeek() {
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);
        List<Post> posts = likeRepository.findTopLikedPostsLastWeek(oneWeekAgo);
        return posts.stream().map(post -> PostResponse.from(post, UserResponse.builder().build())).toList();
    }

    @CachePut(value = "dailyTopKeywords", key = "'latest'")
    public Set<String> getDailyTop10Keywords() {
        LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);
        return searchRepository.findTop10PopularKeywordsInOneDay(oneDayAgo);
    }

    @CachePut(value = "userAvgEarnings", key = "'latest'")
    public BigDecimal getAverageEarningsPerUser() {
        List<Object[]> results = paymentRepository.findTotalSalesAndUniqueUserCount();

        if (results == null || results.isEmpty() || results.get(0) == null) {
            return BigDecimal.ZERO;
        }

        Object[] result = results.get(0);

        BigDecimal totalSales;
        Object sumResult = result[0];
        if (sumResult == null) {
            totalSales = BigDecimal.ZERO;
        } else if (sumResult instanceof BigDecimal) {
            totalSales = (BigDecimal) sumResult;
        } else {
            totalSales = BigDecimal.ZERO;
        }

        Long uniqueUserCount = (Long) result[1];

        if (totalSales.compareTo(BigDecimal.ZERO) == 0 || uniqueUserCount == null || uniqueUserCount == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal divisor = BigDecimal.valueOf(uniqueUserCount);

        return totalSales.divide(divisor, 2, RoundingMode.HALF_UP);
    }

    public List<PostResponse> getPersonalizedRecommendations() {

        User currentUser = authenticationService.getCurrentUser();

        List<Post> posts = postRepository.findRecommendationCandidates(currentUser.getId());
        List<PostResponse> candidates = posts.stream().map(post -> PostResponse.from(post, userService.mapToUserResponse(post.getUser()))).collect(Collectors.toList());

        if (candidates.isEmpty()) {
            return getTopLikedProductsLastWeek();
        }

        Map<ProductCategory, Long> likeScores = getLikeScores(currentUser.getId());
        Map<String, Double> searchScores = getSearchScores(currentUser.getId());

        List<RecommendedPostDto> scoredPosts = new ArrayList<>();

        for (PostResponse post : candidates) {
            double sLike = calculateLikeScore(post, likeScores);
            double sSearch = calculateSearchScore(post, searchScores);

            double score = W_LIKE * sLike + W_SEARCH * sSearch;

            if (score > 0) {
                scoredPosts.add(new RecommendedPostDto(post, score));
            }
        }

        return scoredPosts.stream()
                .sorted(Comparator.comparingDouble(RecommendedPostDto::getScore).reversed())
                .limit(20)
                .map(RecommendedPostDto::getPost)
                .collect(Collectors.toList());
    }

    private Map<ProductCategory, Long> getLikeScores(Long userId) {
        List<Object[]> results = likeRepository.findLikedCategoryScoresAndPostIds(userId);

        return results.stream()
                .collect(Collectors.toMap(
                        arr -> {
                            String categoryName = (String) arr[0];
                            return ProductCategory.valueOf(categoryName.toUpperCase());
                        },
                        arr -> (Long) arr[1]
                ));
    }

    private Map<String, Double> getSearchScores(Long userId) {
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusDays(TIME_PERIOD_DAYS);
        List<Object[]> results = searchRepository.findRecentKeywordsWithTime(userId, oneMonthAgo);

        Map<String, Double> weightedScores = new HashMap<>();

        for (Object[] result : results) {
            String keyword = (String) result[0];
            Object timeObject = result[1];
            LocalDateTime createdAt;

            if (timeObject instanceof java.sql.Timestamp) {
                createdAt = ((java.sql.Timestamp) timeObject).toLocalDateTime();
            } else if (timeObject instanceof java.time.LocalDateTime) {
                createdAt = (java.time.LocalDateTime) timeObject;
            } else {
                throw new IllegalArgumentException("지원하지 않는 시간 타입입니다: " + timeObject.getClass().getName());
            }

            long daysSinceSearch = ChronoUnit.DAYS.between(createdAt, LocalDateTime.now());
            double timeWeight = 1.0 - ((double) daysSinceSearch / TIME_PERIOD_DAYS);

            // 키워드별 가중치 합산 (동일 키워드라도 여러 번 검색했다면 합산)
            weightedScores.merge(keyword, timeWeight, Double::sum);
        }
        return weightedScores;
    }

    // 좋아요 기반 점수 계산 (예시: 카테고리 일치 시 빈도수 점수 부여)
    private double calculateLikeScore(PostResponse post, Map<ProductCategory, Long> likeScores) {
        ProductCategory category = post.getCategory();
        return likeScores.getOrDefault(category, 0L).doubleValue();
    }

    // 검색 기반 점수 계산 (예시: 해시태그와 검색 키워드 일치 시 가중치 점수 부여)
    private double calculateSearchScore(PostResponse post, Map<String, Double> searchScores) {
        double score = 0.0;

        for (Map.Entry<String, Double> entry : searchScores.entrySet()) {
            if (post.getTitle().toLowerCase().contains(entry.getKey().toLowerCase())) {
                score += entry.getValue();
            }
        }

        for (HashtagResponse tag : post.getHashtags()) {
            score += searchScores.getOrDefault(tag.getName(), 0.0);
        }
        return score;
    }
}
