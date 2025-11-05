package com.rebay.rebay_backend.Post.repository;

import com.rebay.rebay_backend.Post.entity.Hashtag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HashTagRepository extends JpaRepository<Hashtag, Long> {
    Optional<Hashtag> findByName(String name);
}

