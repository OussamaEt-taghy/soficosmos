package ma.sofisoft.exceptions.roles;

import jakarta.ws.rs.core.Response;
import lombok.Getter;
import ma.sofisoft.exceptions.BusinessException;

@Getter
public class RoleCreationException extends BusinessException {
    public RoleCreationException(String roleName) {
        super("Impossible de créer le rôle '" + roleName + "'.",
                "ROLE_CREATION_FAILED",
                Response.Status.BAD_REQUEST.getStatusCode());
    }
}
