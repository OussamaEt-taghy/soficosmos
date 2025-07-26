package ma.sofisoft.exceptions.autorisations;

import jakarta.ws.rs.core.Response;
import ma.sofisoft.exceptions.BusinessException;

public class AutorisationAlreadyExistsException extends BusinessException {
    public AutorisationAlreadyExistsException(String permissionName, String roleId) {
        super(
                "L'autorisation entre la permission '" + permissionName + "' et le rôle '" + roleId + "' existe déjà.",
                "AUTORISATION_ALREADY_EXISTS",
                Response.Status.CONFLICT.getStatusCode()
        );
    }
}
