package com.example.phuongstore.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Ném ra khi không tìm thấy tài nguyên theo ID.
 * Tự động trả về HTTP 404.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resourceName, Long id) {
        super("Không tìm thấy %s với ID: %d".formatted(resourceName, id));
    }

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
