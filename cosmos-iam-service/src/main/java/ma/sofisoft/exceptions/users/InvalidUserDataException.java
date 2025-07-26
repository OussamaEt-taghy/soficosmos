package ma.sofisoft.exceptions.users;

import jakarta.ws.rs.core.Response;
import lombok.Getter;
import ma.sofisoft.exceptions.BusinessException;
@Getter
public class InvalidUserDataException extends BusinessException {
    public InvalidUserDataException(String reason) {
        super("Données utilisateur invalides : " + reason,
                "INVALID_USER_DATA",
                Response.Status.BAD_REQUEST.getStatusCode());
    }
}
