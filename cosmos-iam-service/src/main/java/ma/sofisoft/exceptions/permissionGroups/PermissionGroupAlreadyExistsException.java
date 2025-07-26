package ma.sofisoft.exceptions.permissionGroups;

import jakarta.ws.rs.core.Response;
import ma.sofisoft.exceptions.BusinessException;

public class PermissionGroupAlreadyExistsException extends BusinessException {
    public PermissionGroupAlreadyExistsException(String groupName) {
        super(
                "Le groupe de permissions '" + groupName + "' existe déjà.",
                "PERMISSION_GROUP_ALREADY_EXISTS",
                Response.Status.CONFLICT.getStatusCode()
        );
    }
}
