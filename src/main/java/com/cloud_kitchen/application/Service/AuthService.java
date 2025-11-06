package com.cloud_kitchen.application.Service;

import com.cloud_kitchen.application.DTO.*;
import com.cloud_kitchen.application.Entity.*;
import com.cloud_kitchen.application.Repository.AdminRepository;
import com.cloud_kitchen.application.Repository.ChefRepository;
import com.cloud_kitchen.application.Repository.StudentRepository;
import com.cloud_kitchen.application.Repository.UserRepository;
import com.cloud_kitchen.application.Security.JwtTokenProvider;
import com.cloud_kitchen.application.Security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final ChefRepository chefRepository;
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final EmailService emailService;

    @Transactional
    public AuthResponse registerStudent(StudentRegistrationRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new RuntimeException("Phone number already exists");
        }

        if (studentRepository.existsByStudentId(request.getStudentId())) {
            throw new RuntimeException("Student ID already exists");
        }

        Student student = new Student();
        student.setEmail(request.getEmail());
        student.setPassword(passwordEncoder.encode(request.getPassword()));
        student.setName(request.getName());
        student.setPhoneNumber(request.getPhoneNumber());
        student.setRole(Role.STUDENT);
        student.setStudentId(request.getStudentId());
        student.setCollege(request.getCollege());
        student.setHostelName(request.getHostelName());
        student.setRoomNumber(request.getRoomNumber());
        student.setAddress(request.getAddress());
        student.setActive(true);

        Student savedStudent = studentRepository.save(student);

        String subject = "Welcome to Cloud Kitchen!";

        String body = """
Hi %s,

Welcome to Cloud Kitchen!

Your student account has been successfully created. You can now log in using your registered email and start exploring our services.

If you have any questions or need assistance, feel free to contact our support team.

Best regards,  
Cloud Kitchen Team
""".formatted(savedStudent.getName());




        emailService.sendEmail(request.getEmail(), subject, body);

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtTokenProvider.generateToken(authentication);

        return new AuthResponse(jwt, savedStudent.getId(), savedStudent.getEmail(),
                savedStudent.getName(), savedStudent.getRole().name());
    }

    @Transactional
    public AuthResponse registerChef(ChefRegistrationRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new RuntimeException("Phone number already exists");
        }

        Chef chef = new Chef();
        chef.setEmail(request.getEmail());
        chef.setPassword(passwordEncoder.encode(request.getPassword()));
        chef.setName(request.getName());
        chef.setPhoneNumber(request.getPhoneNumber());
        chef.setRole(Role.CHEF);
        chef.setSpecialization(request.getSpecialization());
        chef.setExperienceYears(request.getExperienceYears());
        chef.setBio(request.getBio());
        chef.setActive(true);
        chef.setVerified(false);
        chef.setRating(0.0);

        Chef savedChef = chefRepository.save(chef);

        String subject = "Welcome to Cloud Kitchen!";

        String body = """
Hi %s,

Welcome to Cloud Kitchen!

Your chef account has been successfully created. You can now log in using your registered email and start managing your dishes and orders.

If you have any questions or need assistance, feel free to contact our support team.

Best regards,  
Cloud Kitchen Team
""".formatted(savedChef.getName());

        emailService.sendEmail(request.getEmail(), subject, body);


        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtTokenProvider.generateToken(authentication);

        return new AuthResponse(jwt, savedChef.getId(), savedChef.getEmail(),
                savedChef.getName(), savedChef.getRole().name());
    }

    @Transactional
    public AuthResponse registerAdmin(AdminRegistrationRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new RuntimeException("Phone number already exists");
        }

        Admin admin = new Admin();
        admin.setEmail(request.getEmail());
        admin.setPassword(passwordEncoder.encode(request.getPassword()));
        admin.setName(request.getName());
        admin.setPhoneNumber(request.getPhoneNumber());
        admin.setRole(Role.ADMIN);
        admin.setDepartment(request.getDepartment());
        admin.setDesignation(request.getDesignation());
        admin.setActive(true);
        admin.setSuperAdmin(false);

        Admin savedAdmin = adminRepository.save(admin);

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtTokenProvider.generateToken(authentication);

        return new AuthResponse(jwt, savedAdmin.getId(), savedAdmin.getEmail(),
                savedAdmin.getName(), savedAdmin.getRole().name());
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtTokenProvider.generateToken(authentication);

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        
        // Get verified status for chefs
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        Boolean verified = null;
        if (user instanceof Chef) {
            verified = ((Chef) user).getVerified();
        }

        return new AuthResponse(jwt, userPrincipal.getId(), userPrincipal.getEmail(),
                userPrincipal.getName(), userPrincipal.getRole(), verified);
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
