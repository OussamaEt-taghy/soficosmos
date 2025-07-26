package ma.sofisoft.configurations.multitenancy;
import io.agroal.api.AgroalDataSource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import ma.sofisoft.exceptions.organizations.SchemaNotFoundException;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import java.sql.*;

@ApplicationScoped
@Slf4j
public class SchemaMultiTenantConnectionProvider implements MultiTenantConnectionProvider {
    @Inject
    AgroalDataSource dataSource;
    @Override
    public Connection getConnection(Object tenantIdentifier) throws SQLException {
        if (tenantIdentifier == null) {
            throw new IllegalArgumentException("Le tenantIdentifier ne peut pas être null.");
        }
        String schema = tenantIdentifier.toString();
        try {
            Connection conn = dataSource.getConnection();
            if (!schemaExists(conn, schema)) {
                conn.close();
                throw new SchemaNotFoundException(schema);
            }
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("SET search_path TO " + schema);
            }
            log.debug("Connexion configurée pour schéma: {}", schema);
            return conn;
        } catch (SchemaNotFoundException e) {
            throw e;
        } catch (SQLException e) {
            log.error("Erreur configuration connexion pour schéma '{}': {}", schema, e.getMessage());
            throw e;
        }
    }

    @Override
    public void releaseConnection(Object tenantIdentifier, Connection connection) throws SQLException {
        try {
            log.debug("Connexion fermée pour tenant: {}", tenantIdentifier);
        } finally {
            connection.close();
        }
    }

    @Override
    public Connection getAnyConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public void releaseAnyConnection(Connection connection) throws SQLException {
        connection.close();
    }

    @Override
    public boolean supportsAggressiveRelease() {
        return false;
    }

    @Override
    public boolean isUnwrappableAs(Class<?> unwrapType) {
        return false;
    }

    @Override
    public <T> T unwrap(Class<T> unwrapType) {
        return null;
    }


    private boolean schemaExists(Connection conn, String schemaName) throws SQLException {
        String sql = "SELECT 1 FROM information_schema.schemata WHERE schema_name = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, schemaName);
            try (ResultSet rs = stmt.executeQuery()) {
                boolean exists = rs.next();
                log.debug("🔍 Schéma '{}' existe: {}", schemaName, exists ? "OUI" : "NON");
                return exists;
            }
        }
    }
}
