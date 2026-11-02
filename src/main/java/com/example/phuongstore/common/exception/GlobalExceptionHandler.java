package com.example.phuongstore.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Xá»­ lĂ½ ngoáº¡i lá»‡ táº­p trung cho toĂ n bá»™ REST API.
 * Chuáº©n hĂ³a format response lá»—i Ä‘á»ƒ frontend dá»… xá»­ lĂ½.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ---------------------------------------------------------------
    // Standard error response body
    // ---------------------------------------------------------------

    record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        Object details      // null hoáº·c Map<field, message> cho validation errors
    ) {}

    // ---------------------------------------------------------------
    // 404 â€” Resource Not Found
    // ---------------------------------------------------------------

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                ex.getMessage(),
                null
            ));
    }

    // ---------------------------------------------------------------
    // 400 â€” Business Rule Violation
    // ---------------------------------------------------------------

    @ExceptionHandler(org.springframework.security.core.AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(org.springframework.security.core.AuthenticationException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized",
                "Tên đăng nhập hoặc mật khẩu không chính xác.",
                null
            ));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Business Rule Violation",
                ex.getMessage(),
                null
            ));
    }

    // ---------------------------------------------------------------
    // 400 â€” Jakarta Bean Validation failures
    // ---------------------------------------------------------------

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Validation Failed",
                "Dá»¯ liá»‡u Ä‘áº§u vĂ o khĂ´ng há»£p lá»‡. Vui lĂ²ng kiá»ƒm tra láº¡i.",
                fieldErrors
            ));
    }

    // ---------------------------------------------------------------
    // 500 â€” Unhandled exceptions (safety net)
    // ---------------------------------------------------------------

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "ÄĂ£ xáº£y ra lá»—i ná»™i bá»™. Vui lĂ²ng thá»­ láº¡i sau.",
                null
            ));
    }
}
