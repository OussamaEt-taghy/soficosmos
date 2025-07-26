package ma.sofisoft.exceptions.userGroups;

import jakarta.ws.rs.core.Response;
import lombok.Getter;
import ma.sofisoft.exceptions.BusinessException;

@Getter
public class GroupMembersRetrievalException extends BusinessException {
    public GroupMembersRetrievalException(String groupId) {
        super("Impossible de récupérer les membres du groupe '" + groupId + "'.",
                "GROUP_MEMBERS_RETRIEVAL_FAILED",
                Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }
}
