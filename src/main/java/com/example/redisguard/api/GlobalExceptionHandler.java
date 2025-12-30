package com.example.redisguard.api;

import com.example.redisguard.api.dto.RateLimitResponse;
import com.example.redisguard.api.exception.ProcessingException;
import com.example.redisguard.api.exception.RateLimitExceededException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ProcessingException.class)
    public ResponseEntity<String> handleProcessing(ProcessingException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<RateLimitResponse> handleRateLimit(RateLimitExceededException ex) {
        RateLimitResponse response = new RateLimitResponse(false, ex.getCurrent(), ex.getLimit(), ex.getResetInSeconds());
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(response);
    }
}
