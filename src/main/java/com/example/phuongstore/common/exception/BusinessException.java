package com.example.phuongstore.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * NĂ©m ra khi vi pháº¡m nghiá»‡p vá»¥ (business rule violation).
 * VĂ­ dá»¥: picked_quantity > quantity, duplicate product code, v.v.
 * Tá»± Ä‘á»™ng tráº£ vá» HTTP 400.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
