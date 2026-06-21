package com.telecom.postgrescore.exceptions;

/**
 * Exception thrown when a database connection cannot be established
 * or when the connection pool fails to initialize.
 * 
 * @author seif
 */
public class DatabaseConnectionException extends RuntimeException {

    /**
     * @param message A description of the connection failure
     */
    public DatabaseConnectionException(String message) {
        super(message);
    }

    /**
     * @param message A description of the connection failure
     * @param cause   The underlying exception that caused this failure
     */
    public DatabaseConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
