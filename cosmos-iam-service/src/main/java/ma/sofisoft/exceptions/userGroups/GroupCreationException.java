package ma.sofisoft.exceptions.userGroups;

import lombok.Getter;
import ma.sofisoft.exceptions.BusinessException;
@Getter
public class GroupCreationException extends BusinessException {
    public GroupCreationException(String groupName, int statusCode) {
        super("Impossible de créer le groupe '" + groupName + "'.",
                "GROUP_CREATION_FAILED",
                statusCode);
    }
}

