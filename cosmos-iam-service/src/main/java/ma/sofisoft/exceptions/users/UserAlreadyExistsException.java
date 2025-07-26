package ma.sofisoft.exceptions.users;

import jakarta.ws.rs.core.Response;
import lombok.Getter;
import ma.sofisoft.exceptions.BusinessException;
@Getter
public class UserAlreadyExistsException extends BusinessException {
    public UserAlreadyExistsException(String username) {
        super("L'utilisateur '" + username + "' existe déjà.",
                "USER_ALREADY_EXISTS",
                Response.Status.CONFLICT.getStatusCode());
    }
}
