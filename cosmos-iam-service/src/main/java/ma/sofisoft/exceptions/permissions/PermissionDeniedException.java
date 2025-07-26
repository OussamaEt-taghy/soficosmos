package ma.sofisoft.exceptions.permissions;

import jakarta.ws.rs.core.Response;
import ma.sofisoft.exceptions.BusinessException;

public class PermissionDeniedException extends BusinessException {
    public PermissionDeniedException(String requiredPermission) {
        super(
                "Permission requise manquante : '" + requiredPermission + "'",
                "PERMISSION_DENIED",
                Response.Status.FORBIDDEN.getStatusCode()
        );
    }
}