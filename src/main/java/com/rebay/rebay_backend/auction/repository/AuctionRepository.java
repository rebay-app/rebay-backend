package com.rebay.rebay_backend.auction.repository;

import com.rebay.rebay_backend.auction.entity.Auction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuctionRepository extends JpaRepository<Auction, Long> {

}
