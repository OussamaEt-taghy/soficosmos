package ma.sofisoft.exceptions.users;

import jakarta.ws.rs.core.Response;
import lombok.Getter;
import ma.sofisoft.exceptions.BusinessException;

@Getter
public class UserStatusUpdateException extends BusinessException {
    public UserStatusUpdateException(String userId, boolean enabled) {
        super("Impossible de " + (enabled ? "activer" : "désactiver") + " l'utilisateur '" + userId + "'.",
                "USER_STATUS_UPDATE_FAILED",
                Response.Status.BAD_REQUEST.getStatusCode());
    }
}
