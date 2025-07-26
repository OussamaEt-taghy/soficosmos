package ma.sofisoft.exceptions.users;

import jakarta.ws.rs.core.Response;
import lombok.Getter;
import ma.sofisoft.exceptions.BusinessException;

@Getter
public class UserPasswordUpdateException extends BusinessException {
    public UserPasswordUpdateException(String userId) {
        super("Impossible de mettre à jour le mot de passe de l'utilisateur '" + userId + "'.",
                "USER_PASSWORD_UPDATE_FAILED",
                Response.Status.BAD_REQUEST.getStatusCode());
    }
}
