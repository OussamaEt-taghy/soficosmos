package ma.sofisoft.exceptions.organizations;

import jakarta.ws.rs.core.Response;
import lombok.Getter;
import ma.sofisoft.exceptions.BusinessException;

@Getter
public class SchemaNotFoundException extends BusinessException {
    public SchemaNotFoundException(String schemaName) {
        super("Le schéma d'organisation '" + schemaName + "' n'existe pas dans la base de données",
                "SCHEMA_NOT_FOUND",
                Response.Status.NOT_FOUND.getStatusCode());
    }
}
