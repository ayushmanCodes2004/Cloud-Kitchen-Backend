package com.cloud_kitchen.application.Service;

import com.cloud_kitchen.application.DTO.UserResponse;
import com.cloud_kitchen.application.Entity.Chef;
import com.cloud_kitchen.application.Entity.User;
import com.cloud_kitchen.application.Repository.ChefRepository;
import com.cloud_kitchen.application.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ChefRepository chefRepository;

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return convertToResponse(user);
    }

    @Transactional
    public UserResponse deactivateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setActive(false);
        User updatedUser = userRepository.save(user);
        return convertToResponse(updatedUser);
    }

    @Transactional
    public UserResponse activateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setActive(true);
        User updatedUser = userRepository.save(user);
        return convertToResponse(updatedUser);
    }

    public List<UserResponse> getAllChefs() {
        return chefRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserResponse verifyChef(Long chefId) {
        Chef chef = chefRepository.findById(chefId)
                .orElseThrow(() -> new RuntimeException("Chef not found"));
        chef.setVerified(true);
        Chef updatedChef = chefRepository.save(chef);
        return convertToResponse(updatedChef);
    }

    private UserResponse convertToResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setName(user.getName());
        response.setPhoneNumber(user.getPhoneNumber());
        response.setRole(user.getRole().name());
        response.setActive(user.getActive());
        response.setCreatedAt(user.getCreatedAt());
        return response;
    }
}
