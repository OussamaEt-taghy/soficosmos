package ma.sofisoft.exceptions.roles;

import jakarta.ws.rs.core.Response;
import lombok.Getter;
import ma.sofisoft.exceptions.BusinessException;

@Getter
public class RoleDeletionException extends BusinessException {
    public RoleDeletionException(String roleId) {
        super("Impossible de supprimer le rôle '" + roleId + "'.",
                "ROLE_DELETION_FAILED",
                Response.Status.BAD_REQUEST.getStatusCode());
    }
}
