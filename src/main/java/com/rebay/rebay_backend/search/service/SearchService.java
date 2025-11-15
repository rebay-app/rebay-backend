package com.rebay.rebay_backend.search.service;

import com.rebay.rebay_backend.Post.entity.Post;
import com.rebay.rebay_backend.Post.repository.PostRepository;
import com.rebay.rebay_backend.search.entity.Search;
import com.rebay.rebay_backend.search.entity.SearchTarget;
import com.rebay.rebay_backend.search.repository.SearchRepository;
import com.rebay.rebay_backend.user.entity.User;
import com.rebay.rebay_backend.user.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final PostRepository postRepository;
    private final SearchRepository searchRepository;
    private final AuthenticationService authenticationService;

    public Page<Post> searchPost(String keyword, SearchTarget target, Pageable pageable) {
        User currentUser = authenticationService.getCurrentUser();

        String kw = (keyword == null) ? "" : keyword.trim();
        if (kw.isEmpty()) return Page.empty(pageable);
        Search search = Search.builder()
                .user(currentUser)
                .keyword(kw)
                .createdAt(LocalDateTime.now())
                .build();

        searchRepository.save(search);

        return switch (target) {
            case TITLE -> postRepository.findByTitleContains(kw, pageable);
            case USERNAME -> postRepository.findByUsernameContains(kw, pageable);
            case HASHTAG -> postRepository.findByHashtagExact(kw, pageable);
        };
    }

    public Set<String> getSearchHistory() {
        User currentUser = authenticationService.getCurrentUser();
        List<Search> history = searchRepository.findByUserIdOrderByCreatedAt(currentUser.getId());
        Set<String> keywordList = history.stream()
                .map(Search::getKeyword)
                .collect(Collectors.toSet());

        return keywordList;
    }
}
