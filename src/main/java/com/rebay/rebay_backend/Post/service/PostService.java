package com.rebay.rebay_backend.Post.service;

import com.rebay.rebay_backend.Post.dto.PostRequest;
import com.rebay.rebay_backend.Post.dto.PostResponse;
import com.rebay.rebay_backend.Post.entity.Post;
import com.rebay.rebay_backend.Post.entity.SaleStatus;
import com.rebay.rebay_backend.Post.exception.UnauthorizedException;
import com.rebay.rebay_backend.Post.repository.PostRepository;
import com.rebay.rebay_backend.user.entity.User;
import com.rebay.rebay_backend.user.exception.ResourceNotFoundException;
import com.rebay.rebay_backend.user.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final AuthenticationService authenticationService;


    public PostResponse createPost(PostRequest request) {

        User currentUser = authenticationService.getCurrentUser();

        Post post = Post.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .price(request.getPrice())
                .category(request.getCategory())
                .imageUrl(request.getImageUrl())
                .status(SaleStatus.ON_SALE)
                .user(currentUser)
                .build();

        Post savedPost = postRepository.save(post);

        return PostResponse.from(savedPost);
    }

    @Transactional(readOnly = true)
    public Page<PostResponse> getPosts(Pageable pageable) {
        User currentUser = authenticationService.getCurrentUser();

        Page<Post> posts = postRepository.findAllWithUser(pageable);
        return posts.map(post -> {
            PostResponse response = PostResponse.from(post);
            return response;
        });
    }

    @Transactional(readOnly = true)
    public Page<PostResponse> getUserPost(Long userId, Pageable pageable) {
        User currentUser = authenticationService.getCurrentUser();
        Page<Post> posts = postRepository.findByUserId(userId, pageable);
        return posts.map(post -> {
            PostResponse response = PostResponse.from(post);
            return response;
        });
    }


    //조회수 증가하면서 게시글 상세 조회
    @Transactional
    public PostResponse updateViewcount(Long postId) {
        postRepository.updateView(postId);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        return PostResponse.from(post);
    }


    public PostResponse updatePost(Long postId, PostRequest request) {

        User currentUser = authenticationService.getCurrentUser();

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        if (!post.getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You are not authorized to update this post");
        }

        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setPrice(request.getPrice());
        post.setCategory(request.getCategory());
        post.setStatus(request.getStatus() == null ? SaleStatus.ON_SALE: request.getStatus() );
        post.setImageUrl(request.getImageUrl());

        Post updatedPost = postRepository.save(post);
        return PostResponse.from(updatedPost);
    }

    public void deletePost(Long postId) {

        User currentUser = authenticationService.getCurrentUser();

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        if (!post.getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You are not authorized to update this post");
        }

        postRepository.delete(post);
    }


}


