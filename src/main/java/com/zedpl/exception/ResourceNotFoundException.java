package com.zedpl.exception;

/**
 * Thrown when a requested resource does not exist in the database.
 * Mapped to HTTP 404 by GlobalExceptionHandler.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public static ResourceNotFoundException forEntity(String entity, long id) {
        return new ResourceNotFoundException(entity + " not found with id: " + id);
    }

    public static ResourceNotFoundException forEntity(String entity, String field, String value) {
        return new ResourceNotFoundException(entity + " not found with " + field + ": " + value);
    }
}
