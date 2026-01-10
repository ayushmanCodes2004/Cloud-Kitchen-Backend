package com.cloud_kitchen.application.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MenuItemResponse {
    private Long id;
    private String name;
    private String description;
    private Double price;
    private String category;
    private String imageUrl;
    private Boolean available;
    private Boolean vegetarian;
    private Integer preparationTime;
    private String chefName;
    private Long chefId;
    private Boolean chefVerified;
    private Double chefAverageRating;
    private Long chefTotalRatings;
    private Double menuItemAverageRating;
    private Long menuItemTotalRatings;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    
    public Boolean getAvailable() { return available; }
    public void setAvailable(Boolean available) { this.available = available; }
    
    public Boolean getVegetarian() { return vegetarian; }
    public void setVegetarian(Boolean vegetarian) { this.vegetarian = vegetarian; }
    
    public Integer getPreparationTime() { return preparationTime; }
    public void setPreparationTime(Integer preparationTime) { this.preparationTime = preparationTime; }
    
    public String getChefName() { return chefName; }
    public void setChefName(String chefName) { this.chefName = chefName; }
    
    public Long getChefId() { return chefId; }
    public void setChefId(Long chefId) { this.chefId = chefId; }
    
    public Boolean getChefVerified() { return chefVerified; }
    public void setChefVerified(Boolean chefVerified) { this.chefVerified = chefVerified; }
    
    public Double getChefAverageRating() { return chefAverageRating; }
    public void setChefAverageRating(Double chefAverageRating) { this.chefAverageRating = chefAverageRating; }
    
    public Long getChefTotalRatings() { return chefTotalRatings; }
    public void setChefTotalRatings(Long chefTotalRatings) { this.chefTotalRatings = chefTotalRatings; }
    
    public Double getMenuItemAverageRating() { return menuItemAverageRating; }
    public void setMenuItemAverageRating(Double menuItemAverageRating) { this.menuItemAverageRating = menuItemAverageRating; }
    
    public Long getMenuItemTotalRatings() { return menuItemTotalRatings; }
    public void setMenuItemTotalRatings(Long menuItemTotalRatings) { this.menuItemTotalRatings = menuItemTotalRatings; }
}
