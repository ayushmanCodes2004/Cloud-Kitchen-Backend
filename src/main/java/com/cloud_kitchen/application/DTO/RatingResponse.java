package com.cloud_kitchen.application.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RatingResponse {
    private Long id;
    private Integer rating;
    private String comment;
    private String studentName;
    private LocalDateTime createdAt;
}