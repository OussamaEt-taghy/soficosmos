package ma.sofisoft.exceptions.users;

import jakarta.ws.rs.core.Response;
import lombok.Getter;
import ma.sofisoft.exceptions.BusinessException;

@Getter
public class UserGroupsRetrievalException extends BusinessException {
    public UserGroupsRetrievalException(String userId) {
        super("Impossible de récupérer les groupes de l'utilisateur '" + userId + "'.",
                "USER_GROUPS_RETRIEVAL_FAILED",
                Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }
}
