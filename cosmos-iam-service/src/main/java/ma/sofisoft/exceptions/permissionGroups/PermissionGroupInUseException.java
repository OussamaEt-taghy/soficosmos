package ma.sofisoft.exceptions.permissionGroups;

import jakarta.ws.rs.core.Response;
import ma.sofisoft.exceptions.BusinessException;

public class PermissionGroupInUseException extends BusinessException {
    public PermissionGroupInUseException(String groupId) {
        super(
                "Impossible de supprimer le groupe '" + groupId + "' car il contient des permissions. Supprimez d'abord les permissions.",
                "PERMISSION_GROUP_IN_USE",
                Response.Status.CONFLICT.getStatusCode()
        );
    } }