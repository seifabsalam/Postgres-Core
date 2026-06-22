package com.telecom.postgrescore;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.telecom.postgrescore.exceptions.DatabaseConnectionException;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import java.io.Closeable;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Core Database Manager class that maintains a connection pool using HikariCP
 * and provides integration helpers with jOOQ.
 * 
 * Credentials are supplied directly via the constructor to allow the library consumer
 * full control over how they manage and load their database configurations.
 * 
 * @author seif
 */
public class Database implements Closeable {

    private final HikariDataSource dataSource;

    /**
     * Constructs a new Database manager and initializes its connection pool.
     * 
     * @param jdbcUrl  The JDBC URL of the PostgreSQL database
     * @param username The database user
     * @param password The database password
     * @throws DatabaseConnectionException if the connection pool cannot be initialized
     */
    public Database(String jdbcUrl, String username, String password) {
        try {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(jdbcUrl);
            config.setUsername(username);
            config.setPassword(password);

            // Connection pool defaults
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(0);
            config.setIdleTimeout(10000);
            config.setConnectionTimeout(30000);
            config.setMaxLifetime(240000);
            config.setInitializationFailTimeout(0);

            this.dataSource = new HikariDataSource(config);
        } catch (Exception e) {
            throw new DatabaseConnectionException("Failed to initialize database connection pool", e);
        }
    }

    /**
     * Obtains a Connection from the pool.
     * 
     * @return Connection from the pool
     * @throws SQLException if a database access error occurs
     * @throws DatabaseConnectionException if the connection pool is closed
     */
    public Connection getConnection() throws SQLException {
        if (dataSource.isClosed()) {
            throw new DatabaseConnectionException("Database connection pool is closed");
        }
        return dataSource.getConnection();
    }

    /**
     * Wraps an active connection in a jOOQ DSLContext.
     * 
     * @param connection The active database connection
     * @return DSLContext instance configured for Postgres
     */
    public static DSLContext getDSLContext(Connection connection) {
        return DSL.using(connection, SQLDialect.POSTGRES);
    }

    /**
     * Returns a jOOQ DSLContext backed directly by the connection pool's DataSource.
     * jOOQ borrows and returns connections automatically per query, preventing connection leaks.
     * 
     * @return DSLContext instance configured for Postgres
     */
    public DSLContext getDSLContext() {
        return DSL.using(dataSource, SQLDialect.POSTGRES);
    }

    /**
     * Closes the connection pool and all active connections.
     */
    @Override
    public void close() {
        if (!dataSource.isClosed()) {
            dataSource.close();
        }
    }
}
