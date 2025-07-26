package ma.sofisoft.exceptions.autorisations;
import jakarta.ws.rs.core.Response;
import ma.sofisoft.exceptions.BusinessException;

public class PermissionAssignmentException extends BusinessException {
    public PermissionAssignmentException(String permissionName, String roleName) {
        super(
                "Impossible d'associer la permission '" + permissionName + "' au rôle '" + roleName + "'.",
                "PERMISSION_ASSIGNMENT_FAILED",
                Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()
        );
    }
}
