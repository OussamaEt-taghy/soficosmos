package ma.sofisoft.services.roles;
import ma.sofisoft.dtos.userGroup.RemoveRoleFromKeycloakGroupRequest;
import ma.sofisoft.dtos.roles.*;
import ma.sofisoft.dtos.users.UserResponse;
import java.util.List;

public interface RoleServices {
    void createRealmRole(String token, RoleCreationRequest request);
    void addRoleToGroup(String token, AddRoleToKeycloakGroupRequest request);
    void removeRoleFromGroup(String token, RemoveRoleFromKeycloakGroupRequest request);
    UserRolesListResponse getUserRoles(String token, String userId);
    GroupRolesListResponse getGroupRoles(String token, String groupId);
    List<RoleResponse> getRealmRoles(String token);
    void addRoleToUser(String token, String userId, String roleId);
    void removeRoleFromUser(String token, RemoveUserRoleRequest request);
    void deleteRole(String token, String roleId);
    List<UserResponse> getRoleUsers(String token, String roleId);
}

