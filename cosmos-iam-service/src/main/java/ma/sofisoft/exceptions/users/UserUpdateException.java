package ma.sofisoft.exceptions.users;

import jakarta.ws.rs.core.Response;
import lombok.Getter;
import ma.sofisoft.exceptions.BusinessException;

@Getter
public class UserUpdateException extends BusinessException {
    public UserUpdateException(String userId) {
        super("Impossible de mettre à jour l'utilisateur '" + userId + "'.",
                "USER_UPDATE_FAILED",
                Response.Status.BAD_REQUEST.getStatusCode());
    }
}