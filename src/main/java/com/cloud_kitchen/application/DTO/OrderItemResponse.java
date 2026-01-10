package com.cloud_kitchen.application.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemResponse {
    private Long id;
    private Long menuItemId;
    private String menuItemName;
    private Integer quantity;
    private Double price;
    private Double subtotal;
    private Boolean vegetarian; // Added for veg/non-veg icon display
    private Long chefId; // Added to identify the chef for each item
    private String chefName;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getMenuItemId() { return menuItemId; }
    public void setMenuItemId(Long menuItemId) { this.menuItemId = menuItemId; }

    public String getMenuItemName() { return menuItemName; }
    public void setMenuItemName(String menuItemName) { this.menuItemName = menuItemName; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public Double getSubtotal() { return subtotal; }
    public void setSubtotal(Double subtotal) { this.subtotal = subtotal; }

    public Boolean getVegetarian() { return vegetarian; }
    public void setVegetarian(Boolean vegetarian) { this.vegetarian = vegetarian; }

    public Long getChefId() { return chefId; }
    public void setChefId(Long chefId) { this.chefId = chefId; }

    public String getChefName() { return chefName; }
    public void setChefName(String chefName) { this.chefName = chefName; }
}
