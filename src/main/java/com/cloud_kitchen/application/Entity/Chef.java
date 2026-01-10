package com.cloud_kitchen.application.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "chefs")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Chef extends User {

    @Column(nullable = false)
    private String specialization;

    @Column(nullable = false)
    private Integer experienceYears;

    @Column(columnDefinition = "TEXT")
    private String bio;

    private Double rating = 0.0;

    private Boolean verified = false;

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }
    
    public Integer getExperienceYears() { return experienceYears; }
    public void setExperienceYears(Integer experienceYears) { this.experienceYears = experienceYears; }
    
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    
    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }
    
    public Boolean getVerified() { return verified; }
    public void setVerified(Boolean verified) { this.verified = verified; }
}
