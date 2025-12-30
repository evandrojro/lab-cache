package com.example.redisguard.api.exception;

public class ProcessingException extends RuntimeException {
    public ProcessingException(String message) {
        super(message);
    }
}
