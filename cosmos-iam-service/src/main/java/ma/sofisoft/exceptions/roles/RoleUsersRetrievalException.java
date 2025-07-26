package ma.sofisoft.exceptions.roles;

import jakarta.ws.rs.core.Response;
import lombok.Getter;
import ma.sofisoft.exceptions.BusinessException;

@Getter
public class RoleUsersRetrievalException extends BusinessException {
    public RoleUsersRetrievalException(String roleId) {
        super("Impossible de récupérer les utilisateurs ayant le rôle '" + roleId + "'.",
                "ROLE_USERS_RETRIEVAL_FAILED",
                Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }
}