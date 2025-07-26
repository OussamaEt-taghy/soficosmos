package ma.sofisoft.exceptions.users;

import lombok.Getter;
import ma.sofisoft.exceptions.BusinessException;

@Getter
public class UserDeletionException extends BusinessException {
    public UserDeletionException(String userId, int statusCode) {
        super("Impossible de supprimer l'utilisateur '" + userId + "'.",
                "USER_DELETION_FAILED",
                statusCode);
    }
}
