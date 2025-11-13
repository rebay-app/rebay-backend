package com.rebay.rebay_backend.social.service;

import com.rebay.rebay_backend.social.dto.FollowResponse;
import com.rebay.rebay_backend.social.entity.Follow;
import com.rebay.rebay_backend.social.repository.FollowRepository;
import com.rebay.rebay_backend.user.dto.UserResponse;
import com.rebay.rebay_backend.user.entity.User;
import com.rebay.rebay_backend.user.exception.BadRequestException;
import com.rebay.rebay_backend.user.exception.ResourceNotFoundException;
import com.rebay.rebay_backend.user.repository.UserRepository;
import com.rebay.rebay_backend.user.service.AuthenticationService;
import com.rebay.rebay_backend.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final AuthenticationService authenticationService;

    public FollowResponse toggleFollow(Long userId) {
        User currentUser = authenticationService.getCurrentUser();
        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (currentUser.getId().equals(targetUser.getId())) {
            throw new BadRequestException("You cannot follow yourself");
        }

        boolean isFollowing = followRepository.existsByFollowerAndFollowing(currentUser, targetUser);

        if (isFollowing) {
            followRepository.deleteByFollowerAndFollowing(currentUser, targetUser);
            isFollowing = false;
        } else {
            Follow follow = Follow.builder()
                    .follower(currentUser)
                    .following(targetUser)
                    .build();
            followRepository.save(follow);
            isFollowing = true;
        }

        Long followersCount = followRepository.countFollowers(targetUser);
        Long followingCount = followRepository.countFollowing(targetUser);

        return FollowResponse.builder()
                .isFollowing(isFollowing)
                .followersCount(followersCount)
                .followingCount(followingCount)
                .build();
    }

    @Transactional(readOnly = true)
    public FollowResponse getFollowStatus(Long userId) {
        User currentUser = authenticationService.getCurrentUser();
        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        boolean isFollowing = false;
        if(!currentUser.getId().equals(targetUser.getId())) {
            isFollowing = followRepository.existsByFollowerAndFollowing(currentUser, targetUser);
        }

        Long followersCount = followRepository.countFollowers(targetUser);
        Long followingCount = followRepository.countFollowing(targetUser);

        return FollowResponse.builder()
                .isFollowing(isFollowing)
                .followersCount(followersCount)
                .followingCount(followingCount)
                .build();
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> getFollowers(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Page<User> followers = followRepository.findFollowers(user, pageable);

        return followers.map(follower -> {
            return userService.mapToUserResponse(follower);
        });
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> getFollowing(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Page<User> following = followRepository.findFollowing(user, pageable);

        return following.map(followedUser -> {
            return userService.mapToUserResponse(followedUser);
        });
    }

    public Long getFollowersCount(Long userId) {
        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return followRepository.countFollowers(targetUser);
    }

    public Long getFollowingCount(Long userId) {
        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
       return followRepository.countFollowing(targetUser);
    }

    public boolean getIsFollowing(Long userId) {
        User currentUser = authenticationService.getCurrentUser();

        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return followRepository.existsByFollowerAndFollowing(currentUser, targetUser);
    }
}