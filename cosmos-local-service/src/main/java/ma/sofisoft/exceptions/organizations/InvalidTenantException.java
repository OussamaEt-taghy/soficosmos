package ma.sofisoft.exceptions.organizations;

import jakarta.ws.rs.core.Response;
import lombok.Getter;
import ma.sofisoft.exceptions.BusinessException;

@Getter
public class InvalidTenantException extends BusinessException {
    public InvalidTenantException(String reason) {
        super("Schéma d'organisation invalide : " + reason,
                "INVALID_TENANT_SCHEMA",
                Response.Status.BAD_REQUEST.getStatusCode());
    }
}
