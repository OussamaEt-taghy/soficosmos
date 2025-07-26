package ma.sofisoft.exceptions.autorisations;


import jakarta.ws.rs.core.Response;
import ma.sofisoft.exceptions.BusinessException;

public class AutorisationNotFoundException extends BusinessException {
    public AutorisationNotFoundException(String permissionId, String roleId) {
        super(
                "Aucune autorisation trouvée entre la permission '" + permissionId + "' et le rôle '" + roleId + "'.",
                "AUTORISATION_NOT_FOUND",
                Response.Status.NOT_FOUND.getStatusCode()
        );
    }
}
