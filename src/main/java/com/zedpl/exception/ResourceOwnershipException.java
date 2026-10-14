package com.zedpl.exception;

/**
 * Thrown when a sub-resource is requested under a parent it does not belong to.
 * Example: requesting event 7 under match 99 when event 7 actually belongs to match 3.
 * Mapped to HTTP 404 by GlobalExceptionHandler.
 */
public class ResourceOwnershipException extends RuntimeException {

    public ResourceOwnershipException(String message) {
        super(message);
    }
}
