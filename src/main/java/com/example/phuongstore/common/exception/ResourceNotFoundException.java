package com.example.phuongstore.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * NĂ©m ra khi khĂ´ng tĂ¬m tháº¥y resource theo ID.
 * Tá»± Ä‘á»™ng tráº£ vá» HTTP 404.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resourceName, Long id) {
        super("KhĂ´ng tĂ¬m tháº¥y %s vá»›i ID: %d".formatted(resourceName, id));
    }

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
