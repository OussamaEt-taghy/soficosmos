package ma.sofisoft.services.userGroups;
import ma.sofisoft.dtos.userGroup.AddUserToKeycloakGroupRequest;
import ma.sofisoft.dtos.userGroup.CreateKeycloakGroupRequest;
import ma.sofisoft.dtos.userGroup.GroupResponse;
import ma.sofisoft.dtos.userGroup.RemoveUserFromKeycloakGroupRequest;
import ma.sofisoft.dtos.users.UserResponse;
import java.util.List;

public interface GroupServices {
    String createGroup(String token, CreateKeycloakGroupRequest request);
    void deleteGroup(String token, String groupId);
    void addUserToGroup(String token, AddUserToKeycloakGroupRequest request);
    void removeUserFromGroup(String token, RemoveUserFromKeycloakGroupRequest request);
    List<GroupResponse> getAllGroups(String token);
    List<UserResponse> getGroupMembers(String token, String groupId);
}
