package ma.sofisoft.exceptions.roles;
import jakarta.ws.rs.core.Response;

import ma.sofisoft.exceptions.BusinessException;
public class RoleNotFoundException extends BusinessException {
    public RoleNotFoundException(String groupIdOrName) {
        super(
                "Le groupe '" + groupIdOrName + "' est introuvable.",
                "GROUP_NOT_FOUND",
                Response.Status.NOT_FOUND.getStatusCode()
        );
    }
}

