package com.telecom.postgrescore.exceptions;

/**
 * Exception thrown when a database query execution fails.
 * 
 * @author seif
 */
public class QueryExecutionException extends RuntimeException {

    /**
     * @param message A description of the query execution failure
     */
    public QueryExecutionException(String message) {
        super(message);
    }

    /**
     * @param message A description of the query execution failure
     * @param cause   The underlying exception that caused this failure
     */
    public QueryExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}
