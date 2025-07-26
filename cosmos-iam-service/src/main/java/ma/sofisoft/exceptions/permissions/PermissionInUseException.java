package ma.sofisoft.exceptions.permissions;
import jakarta.ws.rs.core.Response;
import ma.sofisoft.exceptions.BusinessException;

public class PermissionInUseException extends BusinessException {
    public PermissionInUseException(String permissionId) {
        super(
                "Impossible de supprimer la permission '" + permissionId + "' car elle est utilisée par des rôles.",
                "PERMISSION_IN_USE",
                Response.Status.CONFLICT.getStatusCode()
        );
    }
}
