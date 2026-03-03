package com.shopjoy.exception;

import lombok.Getter;
import java.util.Map;

/**
 * Exception thrown when one or more items in a batch operation fail.
 * Contains a map of identifiers to the specific exceptions that occurred.
 */
@Getter
public class BatchOperationException extends RuntimeException {
    private final Map<Integer, Exception> failures;

    public BatchOperationException(String message, Map<Integer, Exception> failures) {
        super(message);
        this.failures = failures;
    }
}
