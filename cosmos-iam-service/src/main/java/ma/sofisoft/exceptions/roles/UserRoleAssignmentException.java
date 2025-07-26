package ma.sofisoft.exceptions.roles;
import jakarta.ws.rs.core.Response;
import lombok.Getter;
import ma.sofisoft.exceptions.BusinessException;

@Getter
public class UserRoleAssignmentException extends BusinessException {
    public UserRoleAssignmentException(String userId, String roleName) {
        super("Échec de l’attribution du rôle '" + roleName + "' à l’utilisateur '" + userId + "'",
                "USER_ROLE_ASSIGNMENT_FAILED",
                Response.Status.BAD_REQUEST.getStatusCode());
    }
}
