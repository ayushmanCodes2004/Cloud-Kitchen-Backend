package com.cloud_kitchen.application.Exception;

import com.cloud_kitchen.application.DTO.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

//@RestControllerAdvice
//public class GlobalExceptionHandler {
//
//    @ExceptionHandler(MethodArgumentNotValidException.class)
//    public ResponseEntity<ApiResponse<R>> handleValidationExceptions(MethodArgumentNotValidException ex) {
//        Map<String, String> errors = new HashMap<>();
//        ex.getBindingResult().getAllErrors().forEach((error) -> {
//            String fieldName = ((FieldError) error).getField();
//            String errorMessage = error.getDefaultMessage();
//            errors.put(fieldName, errorMessage);
//        });
//        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//                .body(new ApiResponse<R>(false, "Validation failed", errors));
//    }
//
//    @ExceptionHandler(UsernameNotFoundException.class)
//    public ResponseEntity<ApiResponse<R>> handleUsernameNotFoundException(UsernameNotFoundException ex) {
//        return ResponseEntity.status(HttpStatus.NOT_FOUND)
//                .body(new ApiResponse<R>(false, ex.getMessage()));
//    }
//
//    @ExceptionHandler(BadCredentialsException.class)
//    public ResponseEntity<ApiResponse<R>> handleBadCredentialsException(BadCredentialsException ex) {
//        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                .body(new ApiResponse<R>(false, "Invalid email or password"));
//    }
//
//    @ExceptionHandler(RuntimeException.class)
//    public ResponseEntity<ApiResponse<R>> handleRuntimeException(RuntimeException ex) {
//        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//                .body(new ApiResponse<R>(false, ex.getMessage()));
//    }
//
//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<ApiResponse<R>> handleGlobalException(Exception ex) {
//        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                .body(new ApiResponse<R>(false, "An error occurred: " + ex.getMessage()));
//    }
//}

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(false, "Validation failed", errors));
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleUsernameNotFoundException(UsernameNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse<>(false, ex.getMessage(), null));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Object>> handleBadCredentialsException(BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponse<>(false, "Invalid email or password", null));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Object>> handleRuntimeException(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(false, ex.getMessage(), null));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGlobalException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "An error occurred: " + ex.getMessage(), null));
    }

    // Add specific handler for rating-related exceptions
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(false, ex.getMessage(), null));
    }
}