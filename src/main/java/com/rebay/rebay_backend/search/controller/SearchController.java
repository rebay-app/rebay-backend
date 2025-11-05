package com.rebay.rebay_backend.search.controller;

import com.rebay.rebay_backend.Post.dto.PostResponse;
import com.rebay.rebay_backend.search.entity.SearchTarget;
import com.rebay.rebay_backend.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/search")
public class SearchController {

    private final SearchService searchService;


    @GetMapping("/posts")
    public Page<PostResponse> searchPosts(
            @RequestParam String keyword,
            @RequestParam SearchTarget target,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return searchService.searchPost(keyword, target, pageable)
                .map(PostResponse::from);
    }
}
