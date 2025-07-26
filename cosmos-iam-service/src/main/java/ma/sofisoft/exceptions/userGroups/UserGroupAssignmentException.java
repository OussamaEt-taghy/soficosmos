package ma.sofisoft.exceptions.userGroups;

import jakarta.ws.rs.core.Response;
import lombok.Getter;
import ma.sofisoft.exceptions.BusinessException;
@Getter
public class UserGroupAssignmentException extends BusinessException {
    public UserGroupAssignmentException(String userId, String groupId) {
        super("Impossible d'ajouter l'utilisateur '" + userId + "' au groupe '" + groupId + "'.",
                "USER_GROUP_ASSIGNMENT_FAILED",
                Response.Status.BAD_REQUEST.getStatusCode());
    }
}
