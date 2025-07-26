package ma.sofisoft.exceptions.userGroups;

import jakarta.ws.rs.core.Response;
import lombok.Getter;
import ma.sofisoft.exceptions.BusinessException;
@Getter
public class GroupRoleAssignmentException extends BusinessException {
    public GroupRoleAssignmentException(String groupId, String roleId) {
        super("Échec de l'attribution du rôle '" + roleId + "' au groupe '" + groupId + "'.",
                "GROUP_ROLE_ASSIGNMENT_FAILED",
                Response.Status.BAD_REQUEST.getStatusCode());
    }
}

