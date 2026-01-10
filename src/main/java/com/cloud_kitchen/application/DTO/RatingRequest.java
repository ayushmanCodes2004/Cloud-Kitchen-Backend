package com.cloud_kitchen.application.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RatingRequest {
    private Long chefId;  // Optional - for chef ratings
    
    private Long menuItemId;  // Optional - for menu item ratings

    @NotNull
    private Long orderId;

    @NotNull
    @Min(1) @Max(5)
    private Integer rating; // 1-5

    private String comment;

    public Long getChefId() { return chefId; }
    public void setChefId(Long chefId) { this.chefId = chefId; }

    public Long getMenuItemId() { return menuItemId; }
    public void setMenuItemId(Long menuItemId) { this.menuItemId = menuItemId; }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}