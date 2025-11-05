package com.cloud_kitchen.application.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChefRatingStats {
    private Long chefId;
    private String chefName;
    private Double averageRating;
    private Long totalRatings;
    private List<RatingResponse> ratings;
}