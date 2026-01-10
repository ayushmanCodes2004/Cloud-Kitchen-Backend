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

    public Long getChefId() { return chefId; }
    public void setChefId(Long chefId) { this.chefId = chefId; }

    public String getChefName() { return chefName; }
    public void setChefName(String chefName) { this.chefName = chefName; }

    public Double getAverageRating() { return averageRating; }
    public void setAverageRating(Double averageRating) { this.averageRating = averageRating; }

    public Long getTotalRatings() { return totalRatings; }
    public void setTotalRatings(Long totalRatings) { this.totalRatings = totalRatings; }

    public List<RatingResponse> getRatings() { return ratings; }
    public void setRatings(List<RatingResponse> ratings) { this.ratings = ratings; }
}