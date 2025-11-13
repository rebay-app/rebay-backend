package com.rebay.rebay_backend.statistics.service;

import com.rebay.rebay_backend.Post.dto.PostResponse;
import com.rebay.rebay_backend.Post.entity.Post;
import com.rebay.rebay_backend.search.repository.SearchRepository;
import com.rebay.rebay_backend.social.repository.LikeRepository;
import com.rebay.rebay_backend.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class StatisticsService {
    private final SearchRepository searchRepository;
    private final LikeRepository likeRepository;
    private final UserService userService;

    public Page<PostResponse> getTopLikedProductsLastWeek(Pageable pageable) {
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);
        Page<Post> posts = likeRepository.findTopLikedPostsLastWeek(oneWeekAgo, pageable);
        return posts.map(post -> PostResponse.from(post, userService.mapToUserResponse(post.getUser())));
    }

    public Set<String> getDailyTop10Keywords() {
        LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);
        return searchRepository.findTop10PopularKeywordsInOneDay(oneDayAgo);
    }
}
