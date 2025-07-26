package ma.sofisoft.services.roles;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import ma.sofisoft.dtos.userGroup.RemoveRoleFromKeycloakGroupRequest;
import ma.sofisoft.dtos.roles.*;
import ma.sofisoft.dtos.users.UserResponse;
import ma.sofisoft.services.KeycloakService;
import java.util.List;

@Slf4j
@ApplicationScoped
public class RoleServicesImpl implements RoleServices {

    @Inject
    KeycloakService keycloakService;

    @Override
    public void createRealmRole(String token, RoleCreationRequest request) {
        keycloakService.createRealmRole(token, request);
    }

    @Override
    public void addRoleToGroup(String token, AddRoleToKeycloakGroupRequest request) {
        keycloakService.addRoleToGroup(token, request);
    }

    @Override
    public void removeRoleFromGroup(String token, RemoveRoleFromKeycloakGroupRequest request) {
        keycloakService.removeRoleFromGroup(token, request);
    }

    @Override
    public UserRolesListResponse getUserRoles(String token, String userId) {
        return keycloakService.getUserRoles(token, userId);
    }

    @Override
    public GroupRolesListResponse getGroupRoles(String token, String groupId) {
        return keycloakService.getGroupRoles(token, groupId);
    }

    @Override
    public List<RoleResponse> getRealmRoles(String token) {
        return keycloakService.getRealmRoles(token);
    }

    @Override
    public void addRoleToUser(String token, String userId, String roleId) {
        keycloakService.addRoleToUser(token, userId, roleId);
    }

    @Override
    public void removeRoleFromUser(String token, RemoveUserRoleRequest request) {
        keycloakService.removeRoleFromUser(token, request);
    }

    @Override
    public void deleteRole(String token, String roleId) {
        keycloakService.deleteRole(token, roleId);
    }

    @Override
    public List<UserResponse> getRoleUsers(String token, String roleId) {
        return keycloakService.getRoleUsers(token, roleId);
    }
}