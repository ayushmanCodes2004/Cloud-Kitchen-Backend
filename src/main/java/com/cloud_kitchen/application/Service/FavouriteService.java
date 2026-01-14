package com.cloud_kitchen.application.Service;

import com.cloud_kitchen.application.Entity.Favourite;
import com.cloud_kitchen.application.Entity.MenuItem;
import com.cloud_kitchen.application.Entity.Student;
import com.cloud_kitchen.application.Repository.FavouriteRepository;
import com.cloud_kitchen.application.Repository.MenuItemRepository;
import com.cloud_kitchen.application.Repository.StudentRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class FavouriteService {

    @Autowired
    private FavouriteRepository favouriteRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private MenuItemRepository menuItemRepository;

    public List<Favourite> getStudentFavourites(Long userId) {
        Student student = studentRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Student not found with id: " + userId));
        return favouriteRepository.findByStudentOrderByCreatedAtDesc(student);
    }

    @Transactional
    public Favourite addFavourite(Long userId, Long menuItemId) {
        log.info("Adding favourite - userId: {}, menuItemId: {}", userId, menuItemId);
        
        Student student = studentRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("Student not found with id: {}", userId);
                    return new RuntimeException("Student not found with id: " + userId);
                });
        
        log.info("Found student: {}", student.getEmail());
        
        MenuItem menuItem = menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> {
                    log.error("Menu item not found with id: {}", menuItemId);
                    return new RuntimeException("Menu item not found with id: " + menuItemId);
                });

        log.info("Found menu item: {}", menuItem.getName());

        // Check if already exists
        if (favouriteRepository.existsByStudentAndMenuItemId(student, menuItemId)) {
            log.warn("Menu item {} already in favourites for student {}", menuItemId, userId);
            throw new RuntimeException("Menu item already in favourites");
        }

        Favourite favourite = new Favourite();
        favourite.setStudent(student);
        favourite.setMenuItem(menuItem);
        
        Favourite saved = favouriteRepository.save(favourite);
        log.info("Successfully saved favourite with id: {}", saved.getId());
        
        return saved;
    }

    @Transactional
    public void removeFavourite(Long userId, Long menuItemId) {
        Student student = studentRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Student not found with id: " + userId));
        
        favouriteRepository.deleteByStudentAndMenuItemId(student, menuItemId);
    }

    public boolean isFavourite(Long userId, Long menuItemId) {
        Student student = studentRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Student not found with id: " + userId));
        
        return favouriteRepository.existsByStudentAndMenuItemId(student, menuItemId);
    }

    public List<Long> getFavouriteMenuItemIds(Long userId) {
        Student student = studentRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Student not found with id: " + userId));
        
        return favouriteRepository.findMenuItemIdsByStudent(student);
    }
}
