package com.example.phuongstore.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Ném ra khi vi phạm nghiệp vụ (business rule violation).
 * Ví dụ: picked_quantity > quantity, duplicate product code, v.v.
 * Tự động trả về HTTP 400.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
