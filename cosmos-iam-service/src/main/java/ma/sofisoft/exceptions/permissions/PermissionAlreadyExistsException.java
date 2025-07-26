package ma.sofisoft.exceptions.permissions;

import jakarta.ws.rs.core.Response;
import ma.sofisoft.exceptions.BusinessException;

public class PermissionAlreadyExistsException extends BusinessException {
    public PermissionAlreadyExistsException(String permissionName) {
        super(
                "La permission '" + permissionName + "' existe déjà.",
                "PERMISSION_ALREADY_EXISTS",
                Response.Status.CONFLICT.getStatusCode()
        );
    }
}