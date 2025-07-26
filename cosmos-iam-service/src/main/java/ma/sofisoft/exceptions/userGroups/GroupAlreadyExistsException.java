package ma.sofisoft.exceptions.userGroups;

import jakarta.ws.rs.core.Response;
import lombok.Getter;
import ma.sofisoft.exceptions.BusinessException;
@Getter
public class GroupAlreadyExistsException extends BusinessException {
    public GroupAlreadyExistsException(String groupName) {
        super("Le groupe '" + groupName + "' existe déjà.",
                "GROUP_ALREADY_EXISTS",
                Response.Status.CONFLICT.getStatusCode());
    }
}
