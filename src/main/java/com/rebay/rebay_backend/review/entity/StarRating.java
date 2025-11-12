package com.rebay.rebay_backend.review.entity;

public enum StarRating {
    ONE(1), TWO(2), THREE(3), FOUR(4), FIVE(5);

    private final int value;

    StarRating(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static StarRating of(int value) {
        for (StarRating rating : StarRating.values()) {
            if (rating.value == value) {
                return rating;
            }
        }
        throw new IllegalArgumentException("유효하지 않은 별점 값: " + value);
    }
}
