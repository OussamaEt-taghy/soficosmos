package ma.sofisoft.exceptions.roles;

import jakarta.ws.rs.core.Response;
import lombok.Getter;
import ma.sofisoft.exceptions.BusinessException;

@Getter
public class RoleRetrievalException extends BusinessException {
    public RoleRetrievalException(String organizationName) {
        super("Impossible de récupérer les rôles pour l'organisation '" + organizationName + "'.",
                "ROLE_RETRIEVAL_FAILED",
                Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }
}
