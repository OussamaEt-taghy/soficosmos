package ma.sofisoft.exceptions.userGroups;

import jakarta.ws.rs.core.Response;
import lombok.Getter;
import ma.sofisoft.exceptions.BusinessException;

@Getter
public class GroupRetrievalException extends BusinessException {
    public GroupRetrievalException(String organizationName) {
        super("Impossible de récupérer les groupes pour l'organisation '" + organizationName + "'.",
                "GROUP_RETRIEVAL_FAILED",
                Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }
}
