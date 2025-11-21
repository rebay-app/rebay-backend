package com.rebay.rebay_backend.auction.service;

import com.rebay.rebay_backend.Post.entity.Category;
import com.rebay.rebay_backend.Post.entity.SaleStatus;
import com.rebay.rebay_backend.Post.repository.CategoryRepository;
import com.rebay.rebay_backend.auction.dto.AuctionRequest;
import com.rebay.rebay_backend.auction.dto.AuctionResponse;
import com.rebay.rebay_backend.auction.entity.Auction;
import com.rebay.rebay_backend.auction.repository.AuctionRepository;
import com.rebay.rebay_backend.user.dto.UserResponse;
import com.rebay.rebay_backend.user.entity.User;
import com.rebay.rebay_backend.user.exception.ResourceNotFoundException;
import com.rebay.rebay_backend.user.service.AuthenticationService;
import com.rebay.rebay_backend.user.service.UserService;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuctionService {
    private final UserService userService;
    private final AuthenticationService authenticationService;
    private final CategoryRepository categoryRepository;
    private final AuctionRepository auctionRepository;

    public AuctionResponse createAuction(AuctionRequest request) {
        User currentUser = authenticationService.getCurrentUser();
        Category currentCategory = categoryRepository.findByCode(request.getCategoryCode())
                .orElseThrow(() -> new ResourceNotFoundException("카테고리를 찾을 수 없습니다."));

        Auction auction = Auction.builder()
                .seller(currentUser)
                .title(request.getTitle())
                .content(request.getContent())
                .startPrice(request.getStartPrice())
                .currentPrice(request.getCurrentPrice())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .imageUrl(request.getImageUrl())
                .category(currentCategory)
                .status(request.getStatus())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Auction savedAuction = auctionRepository.save(auction);
        UserResponse userResponse = userService.mapToUserResponse(currentUser);

        return AuctionResponse.fromEntity(savedAuction, userResponse);
    }
}
