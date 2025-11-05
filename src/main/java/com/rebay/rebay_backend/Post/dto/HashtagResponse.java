package com.rebay.rebay_backend.Post.dto;

import com.rebay.rebay_backend.Post.entity.Hashtag;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HashtagResponse {

    private Long id;
    private String name;

    public static HashtagResponse from(Hashtag hashtag) {

        return HashtagResponse.builder()
                .id(hashtag.getId())
                .name(hashtag.getName())
                .build();

    }
}
