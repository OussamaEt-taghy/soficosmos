package ma.sofisoft.exceptions.permissionGroups;

import jakarta.ws.rs.core.Response;
import ma.sofisoft.exceptions.BusinessException;

public class PermissionGroupNotFoundException extends BusinessException {
    public PermissionGroupNotFoundException(String groupIdOrName) {
        super(
                "Le groupe de permissions '" + groupIdOrName + "' est introuvable.",
                "PERMISSION_GROUP_NOT_FOUND",
                Response.Status.NOT_FOUND.getStatusCode()
        );
    }
}
