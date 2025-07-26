package ma.sofisoft.exceptions.roles;

import jakarta.ws.rs.core.Response;
import lombok.Getter;
import ma.sofisoft.exceptions.BusinessException;
@Getter
public class RoleAlreadyExistsException extends BusinessException {
    public RoleAlreadyExistsException(String roleName) {
        super("Le rôle '" + roleName + "' existe déjà.",
                "ROLE_ALREADY_EXISTS",
                Response.Status.CONFLICT.getStatusCode());
    }
}
