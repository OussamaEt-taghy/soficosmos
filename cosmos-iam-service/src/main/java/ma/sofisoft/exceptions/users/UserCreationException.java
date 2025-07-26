package ma.sofisoft.exceptions.users;

import lombok.Getter;
import ma.sofisoft.exceptions.BusinessException;
@Getter
public class UserCreationException extends BusinessException {
    public UserCreationException(String username, int statusCode) {
        super("Impossible de créer l'utilisateur '" + username + "'", "USER_CREATION_FAILED", statusCode);
    }
}

