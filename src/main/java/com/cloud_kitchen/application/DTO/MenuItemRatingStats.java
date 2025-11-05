package com.cloud_kitchen.application.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MenuItemRatingStats {
    private Long menuItemId;
    private String menuItemName;
    private Double averageRating;
    private Long totalRatings;
    private List<RatingResponse> ratings;
}
