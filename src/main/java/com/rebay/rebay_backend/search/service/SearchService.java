package com.rebay.rebay_backend.search.service;

import com.rebay.rebay_backend.Post.entity.Post;
import com.rebay.rebay_backend.Post.repository.PostRepository;
import com.rebay.rebay_backend.search.entity.SearchTarget;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final PostRepository postRepository;

    public Page<Post> searchPost(String keyword, SearchTarget target, Pageable pageable) {

        String kw = (keyword == null) ? "" : keyword.trim();
        if (kw.isEmpty()) return Page.empty(pageable);


        return switch (target) {
            case TITLE -> postRepository.findByTitleContains(kw, pageable);
            case USERNAME -> postRepository.findByUsernameContains(kw, pageable);
            case HASHTAG -> postRepository.findByHashtagExact(kw, pageable);
        };
    }
}
