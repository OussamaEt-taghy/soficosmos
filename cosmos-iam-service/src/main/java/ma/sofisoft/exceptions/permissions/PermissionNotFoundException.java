package ma.sofisoft.exceptions.permissions;

import jakarta.ws.rs.core.Response;
import ma.sofisoft.exceptions.BusinessException;

public class PermissionNotFoundException extends BusinessException {
    public PermissionNotFoundException(String permissionIdOrName) {
        super(
                "La permission '" + permissionIdOrName + "' est introuvable.",
                "PERMISSION_NOT_FOUND",
                Response.Status.NOT_FOUND.getStatusCode()
        );
    }
}
