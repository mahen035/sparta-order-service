package com.training.orderservice.exception;

/**
 * Thrown when a caller is not authorized to perform an operation on an order.
 * Maps to HTTP 403 ORDER_ACCESS_DENIED. Used to enforce BR-11: hard-delete is
 * restricted to admin-role callers.
 */
public class OrderAccessDeniedException extends RuntimeException {
    public OrderAccessDeniedException(String message) {
        super(message);
    }
}
