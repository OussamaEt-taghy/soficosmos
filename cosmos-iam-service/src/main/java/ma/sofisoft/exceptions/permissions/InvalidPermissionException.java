package ma.sofisoft.exceptions.permissions;

import jakarta.ws.rs.core.Response;
import ma.sofisoft.exceptions.BusinessException;

public class InvalidPermissionException extends BusinessException {
    public InvalidPermissionException(String details) {
        super(
                "La permission est invalide : " + details,
                "INVALID_PERMISSION",
                Response.Status.BAD_REQUEST.getStatusCode()
        );
    }
}
