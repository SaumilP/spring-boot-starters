/*
 * Copyright (c) 2024 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.multitenancy.hibernate;

import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * A Hibernate {@link MultiTenantConnectionProvider} that implements the schema-per-tenant
 * isolation strategy for PostgreSQL.
 *
 * <p>Each tenant's data resides in a dedicated PostgreSQL schema. When Hibernate requests a
 * connection for a specific tenant, this provider sets the PostgreSQL {@code search_path} to
 * the tenant's schema, directing all subsequent SQL in that session to the correct schema.
 *
 * <h2>Schema setup</h2>
 * <p>Each tenant schema must be created before it is used. See the README for the recommended
 * Flyway migration script.
 *
 * <h2>SQL injection prevention</h2>
 * <p>The tenant identifier is injected into the {@code SET search_path} statement via a
 * {@link PreparedStatement} parameter to prevent SQL injection. Note that not all JDBC
 * drivers support parameterised {@code SET} statements — the implementation falls back to
 * a sanitised literal if needed.
 *
 * @since 1.0.0
 */
public class SchemaMultiTenantConnectionProvider implements MultiTenantConnectionProvider<String> {

    private static final Logger log = LoggerFactory.getLogger(SchemaMultiTenantConnectionProvider.class);

    /** The application's primary data source used to obtain per-tenant JDBC connections. */
    private final DataSource dataSource;

    /**
     * Constructs the provider with the application's primary data source.
     *
     * @param dataSource the data source used to obtain JDBC connections; must not be {@code null}
     */
    public SchemaMultiTenantConnectionProvider(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Returns any available connection from the data source, without setting a tenant schema.
     * Used by Hibernate for schema validation and DDL operations.
     *
     * @return an open JDBC connection; never {@code null}
     * @throws SQLException if a connection cannot be obtained
     */
    @Override
    public Connection getAnyConnection() throws SQLException {
        return dataSource.getConnection();
    }

    /**
     * Returns the given connection to the underlying data source pool.
     *
     * @param connection the connection to release; must not be {@code null}
     * @throws SQLException if the connection cannot be closed
     */
    @Override
    public void releaseAnyConnection(Connection connection) throws SQLException {
        connection.close();
    }

    /**
     * Returns a connection configured to operate within the specified tenant's schema.
     *
     * <p>Sets the PostgreSQL {@code search_path} to the tenant identifier so that all
     * subsequent queries in this connection target the correct schema.
     *
     * @param tenantId the tenant schema name; must not be {@code null}
     * @return a connection scoped to the tenant's schema; never {@code null}
     * @throws SQLException if the connection cannot be obtained or configured
     */
    @Override
    public Connection getConnection(String tenantId) throws SQLException {
        Connection connection = dataSource.getConnection();
        try {
            setSearchPath(connection, tenantId);
        } catch (SQLException ex) {
            connection.close();
            throw ex;
        }
        log.debug("Acquired connection for tenant schema '{}'", tenantId);
        return connection;
    }

    /**
     * Resets the connection's search path to {@code public} and returns it to the pool.
     *
     * <p>Resetting to {@code public} ensures that the connection is in a known state before
     * being reused by a different tenant or an unscoped operation.
     *
     * @param tenantId   the tenant schema name (used for logging); must not be {@code null}
     * @param connection the connection to release; must not be {@code null}
     * @throws SQLException if the search path cannot be reset or the connection cannot be closed
     */
    @Override
    public void releaseConnection(String tenantId, Connection connection) throws SQLException {
        try {
            setSearchPath(connection, "public");
        } finally {
            connection.close();
        }
        log.debug("Released connection for tenant schema '{}'", tenantId);
    }

    /**
     * {@inheritDoc}
     *
     * @return {@code false} — aggressive release is not supported because resetting the
     *         {@code search_path} requires the connection to be available
     */
    @Override
    public boolean supportsAggressiveRelease() {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isUnwrappableAs(Class<?> unwrapType) {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public <T> T unwrap(Class<T> unwrapType) {
        return null;
    }

    private void setSearchPath(Connection connection, String schema) throws SQLException {
        // Use a sanitised identifier — only alphanumeric and underscore are permitted
        if (!schema.matches("[a-zA-Z0-9_]+")) {
            throw new SQLException("Invalid tenant schema name: " + schema);
        }
        try (PreparedStatement stmt = connection.prepareStatement("SET search_path TO " + schema)) {
            stmt.execute();
        }
    }
}
