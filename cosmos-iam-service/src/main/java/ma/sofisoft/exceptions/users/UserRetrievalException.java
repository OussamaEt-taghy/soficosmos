package ma.sofisoft.exceptions.users;

import jakarta.ws.rs.core.Response;
import lombok.Getter;
import ma.sofisoft.exceptions.BusinessException;

@Getter
public class UserRetrievalException extends BusinessException {
    public UserRetrievalException(String organizationName) {
        super("Impossible de récupérer les utilisateurs pour l'organisation '" + organizationName + "'.",
                "USER_RETRIEVAL_FAILED",
                Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }
}
