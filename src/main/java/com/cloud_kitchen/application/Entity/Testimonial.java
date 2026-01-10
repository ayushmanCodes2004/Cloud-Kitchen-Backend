package com.cloud_kitchen.application.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "testimonials")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Testimonial {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 1000)
    private String content;

    @Column(nullable = false)
    @Min(1) @Max(5)
    private Integer rating;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chef_id")
    private Chef chef;

    @Column(nullable = false)
    private String userName;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UserRole userRole;
    
    @Column
    private String institution; // College name for students

    @Column(nullable = false)
    private Boolean approved = false;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public enum UserRole {
        STUDENT, CHEF
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }

    public Chef getChef() { return chef; }
    public void setChef(Chef chef) { this.chef = chef; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public UserRole getUserRole() { return userRole; }
    public void setUserRole(UserRole userRole) { this.userRole = userRole; }

    public String getInstitution() { return institution; }
    public void setInstitution(String institution) { this.institution = institution; }

    public Boolean getApproved() { return approved; }
    public void setApproved(Boolean approved) { this.approved = approved; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
