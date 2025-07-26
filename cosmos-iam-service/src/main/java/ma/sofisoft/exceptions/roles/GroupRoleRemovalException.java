package ma.sofisoft.exceptions.roles;


import jakarta.ws.rs.core.Response;
import lombok.Getter;
import ma.sofisoft.exceptions.BusinessException;

@Getter
public class GroupRoleRemovalException extends BusinessException {
    public GroupRoleRemovalException(String groupId, String roleName) {
        super("Impossible de retirer le rôle '" + roleName + "' du groupe '" + groupId + "'.",
                "GROUP_ROLE_REMOVAL_FAILED",
                Response.Status.BAD_REQUEST.getStatusCode());
    }
}
