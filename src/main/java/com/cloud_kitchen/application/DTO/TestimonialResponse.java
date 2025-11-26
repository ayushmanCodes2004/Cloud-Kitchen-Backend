package com.cloud_kitchen.application.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestimonialResponse {
    private Long id;
    private String userName;
    private String userRole;
    private String institution;
    private String content;
    private Integer rating;
    private Boolean approved;
    private LocalDateTime createdAt;
}
