package ma.sofisoft.exceptions.permissionGroups;

import jakarta.ws.rs.core.Response;
import ma.sofisoft.exceptions.BusinessException;

public class PermissionAlreadyAssignedException extends BusinessException {
    public PermissionAlreadyAssignedException(String permissionName, String currentGroupName) {
        super(
                "La permission '" + permissionName + "' est déjà assignée au groupe '" + currentGroupName + "'.",
                "PERMISSION_ALREADY_ASSIGNED",
                Response.Status.CONFLICT.getStatusCode()
        );
    }
}
