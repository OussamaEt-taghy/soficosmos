package ma.sofisoft.services.userGroups;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import ma.sofisoft.dtos.userGroup.AddUserToKeycloakGroupRequest;
import ma.sofisoft.dtos.userGroup.CreateKeycloakGroupRequest;
import ma.sofisoft.dtos.userGroup.GroupResponse;
import ma.sofisoft.dtos.userGroup.RemoveUserFromKeycloakGroupRequest;
import ma.sofisoft.dtos.users.UserResponse;
import ma.sofisoft.services.KeycloakService;
import java.util.List;

@Slf4j
@ApplicationScoped
public class GroupServicesImpl implements GroupServices {
    @Inject
    KeycloakService keycloakService;

    @Override
    public String createGroup(String token, CreateKeycloakGroupRequest request) {
        return keycloakService.createGroup(token, request);
    }

    @Override
    public void deleteGroup(String token, String groupId) {
        keycloakService.deleteGroup(token, groupId);
    }

    @Override
    public void addUserToGroup(String token, AddUserToKeycloakGroupRequest request) {
        keycloakService.addUserToGroup(token, request);
    }

    @Override
    public void removeUserFromGroup(String token, RemoveUserFromKeycloakGroupRequest request) {
        keycloakService.removeUserFromGroup(token, request);
    }

    @Override
    public List<GroupResponse> getAllGroups(String token) {
        return keycloakService.getAllGroups(token);
    }

    @Override
    public List<UserResponse> getGroupMembers(String token, String groupId) {
        return keycloakService.getGroupMembers(token, groupId);
    }
}