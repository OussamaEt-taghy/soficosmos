package ma.sofisoft.exceptions.roles;

import jakarta.ws.rs.core.Response;
import lombok.Getter;
import ma.sofisoft.exceptions.BusinessException;

@Getter
public class UserRoleRemovalException extends BusinessException {
    public UserRoleRemovalException(String userId, String roleId) {
        super("Impossible de retirer le rôle '" + roleId + "' de l'utilisateur '" + userId + "'.",
                "USER_ROLE_REMOVAL_FAILED",
                Response.Status.BAD_REQUEST.getStatusCode());
    }
}
