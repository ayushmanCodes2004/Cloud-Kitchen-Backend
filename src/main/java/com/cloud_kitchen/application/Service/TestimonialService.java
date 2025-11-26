package com.cloud_kitchen.application.Service;

import com.cloud_kitchen.application.DTO.TestimonialRequest;
import com.cloud_kitchen.application.DTO.TestimonialResponse;
import com.cloud_kitchen.application.Entity.Chef;
import com.cloud_kitchen.application.Entity.Student;
import com.cloud_kitchen.application.Entity.Testimonial;
import com.cloud_kitchen.application.Repository.ChefRepository;
import com.cloud_kitchen.application.Repository.StudentRepository;
import com.cloud_kitchen.application.Repository.TestimonialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TestimonialService {

    private final TestimonialRepository testimonialRepository;
    private final StudentRepository studentRepository;
    private final ChefRepository chefRepository;

    @Transactional
    public TestimonialResponse submitTestimonial(Long userId, String userType, TestimonialRequest request) {
        // Check if user already has a testimonial
        Testimonial existingTestimonial;
        
        if ("STUDENT".equals(userType)) {
            existingTestimonial = testimonialRepository.findByStudentId(userId).orElse(null);
        } else if ("CHEF".equals(userType)) {
            existingTestimonial = testimonialRepository.findByChefId(userId).orElse(null);
        } else {
            throw new RuntimeException("Invalid user type");
        }

        if (existingTestimonial != null) {
            throw new RuntimeException("You have already submitted a testimonial. Please update it instead.");
        }

        Testimonial testimonial = new Testimonial();
        testimonial.setContent(request.getContent());
        testimonial.setRating(request.getRating());
        testimonial.setApproved(false);

        if ("STUDENT".equals(userType)) {
            Student student = studentRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Student not found"));
            testimonial.setStudent(student);
            testimonial.setUserName(student.getName());
            testimonial.setUserRole(Testimonial.UserRole.STUDENT);
            testimonial.setInstitution(student.getCollege());
        } else {
            Chef chef = chefRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Chef not found"));
            testimonial.setChef(chef);
            testimonial.setUserName(chef.getName());
            testimonial.setUserRole(Testimonial.UserRole.CHEF);
            testimonial.setInstitution(null); // Chefs don't have institution
        }

        Testimonial saved = testimonialRepository.save(testimonial);
        return mapToResponse(saved);
    }

    @Transactional
    public TestimonialResponse updateTestimonial(Long testimonialId, Long userId, String userType, TestimonialRequest request) {
        Testimonial testimonial = testimonialRepository.findById(testimonialId)
                .orElseThrow(() -> new RuntimeException("Testimonial not found"));

        // Verify ownership
        if ("STUDENT".equals(userType) && !testimonial.getStudent().getId().equals(userId)) {
            throw new RuntimeException("You can only update your own testimonial");
        }
        if ("CHEF".equals(userType) && !testimonial.getChef().getId().equals(userId)) {
            throw new RuntimeException("You can only update your own testimonial");
        }

        testimonial.setContent(request.getContent());
        testimonial.setRating(request.getRating());
        testimonial.setApproved(false); // Reset approval status on update

        Testimonial updated = testimonialRepository.save(testimonial);
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteTestimonial(Long testimonialId, Long userId, String userType) {
        Testimonial testimonial = testimonialRepository.findById(testimonialId)
                .orElseThrow(() -> new RuntimeException("Testimonial not found"));

        // Verify ownership
        if ("STUDENT".equals(userType) && !testimonial.getStudent().getId().equals(userId)) {
            throw new RuntimeException("You can only delete your own testimonial");
        }
        if ("CHEF".equals(userType) && !testimonial.getChef().getId().equals(userId)) {
            throw new RuntimeException("You can only delete your own testimonial");
        }

        testimonialRepository.delete(testimonial);
    }

    public TestimonialResponse getMyTestimonial(Long userId, String userType) {
        Testimonial testimonial;
        
        if ("STUDENT".equals(userType)) {
            testimonial = testimonialRepository.findByStudentId(userId).orElse(null);
        } else if ("CHEF".equals(userType)) {
            testimonial = testimonialRepository.findByChefId(userId).orElse(null);
        } else {
            throw new RuntimeException("Invalid user type");
        }

        return testimonial != null ? mapToResponse(testimonial) : null;
    }

    public List<TestimonialResponse> getApprovedTestimonials() {
        return testimonialRepository.findByApprovedTrue().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<TestimonialResponse> getPendingTestimonials() {
        return testimonialRepository.findByApprovedFalse().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public TestimonialResponse approveTestimonial(Long testimonialId) {
        Testimonial testimonial = testimonialRepository.findById(testimonialId)
                .orElseThrow(() -> new RuntimeException("Testimonial not found"));
        
        testimonial.setApproved(true);
        Testimonial updated = testimonialRepository.save(testimonial);
        return mapToResponse(updated);
    }

    @Transactional
    public void rejectTestimonial(Long testimonialId) {
        Testimonial testimonial = testimonialRepository.findById(testimonialId)
                .orElseThrow(() -> new RuntimeException("Testimonial not found"));
        
        testimonialRepository.delete(testimonial);
    }

    private TestimonialResponse mapToResponse(Testimonial testimonial) {
        TestimonialResponse response = new TestimonialResponse();
        response.setId(testimonial.getId());
        response.setUserName(testimonial.getUserName());
        response.setUserRole(testimonial.getUserRole().name());
        response.setInstitution(testimonial.getInstitution());
        response.setContent(testimonial.getContent());
        response.setRating(testimonial.getRating());
        response.setApproved(testimonial.getApproved());
        response.setCreatedAt(testimonial.getCreatedAt());
        return response;
    }
}
