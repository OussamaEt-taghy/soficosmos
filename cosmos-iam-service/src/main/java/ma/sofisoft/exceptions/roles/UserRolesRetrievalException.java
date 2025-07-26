package ma.sofisoft.exceptions.roles;

import jakarta.ws.rs.core.Response;
import lombok.Getter;
import ma.sofisoft.exceptions.BusinessException;

@Getter
public class UserRolesRetrievalException extends BusinessException {
    public UserRolesRetrievalException(String userId) {
        super("Impossible de récupérer les rôles de l'utilisateur '" + userId + "'.",
                "USER_ROLES_RETRIEVAL_FAILED",
                Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }
}
