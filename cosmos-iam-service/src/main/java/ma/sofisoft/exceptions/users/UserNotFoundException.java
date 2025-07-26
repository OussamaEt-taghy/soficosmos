package ma.sofisoft.exceptions.users;

import jakarta.ws.rs.core.Response;
import lombok.Getter;
import ma.sofisoft.exceptions.BusinessException;
@Getter
public class UserNotFoundException extends BusinessException {
    public UserNotFoundException(String username) {
        super("L'utilisateur '" + username + "' est introuvable.",
                "USER_NOT_FOUND",
                Response.Status.NOT_FOUND.getStatusCode());
    }
}
