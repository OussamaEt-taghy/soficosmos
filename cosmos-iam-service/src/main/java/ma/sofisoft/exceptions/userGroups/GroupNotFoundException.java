package ma.sofisoft.exceptions.userGroups;

import jakarta.ws.rs.core.Response;
import lombok.Getter;
import ma.sofisoft.exceptions.BusinessException;
@Getter
public class GroupNotFoundException extends BusinessException {
    public GroupNotFoundException(String groupIdOrName) {
        super("Le groupe '" + groupIdOrName + "' est introuvable.",
                "GROUP_NOT_FOUND",
                Response.Status.NOT_FOUND.getStatusCode());
    }
}

