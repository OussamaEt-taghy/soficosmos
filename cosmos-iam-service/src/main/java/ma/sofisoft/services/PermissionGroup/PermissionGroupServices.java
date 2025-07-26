package ma.sofisoft.services.PermissionGroup;

import ma.sofisoft.dtos.permissionGroup.*;
import java.util.List;
import java.util.UUID;
public interface PermissionGroupServices {
    UUID createPermissionGroup(String token, CreatePermissionGroupRequest request);
    void updatePermissionGroup(String token, UUID groupId, UpdatePermissionGroupRequest request);
    void deletePermissionGroup(String token, UUID groupId);
    List<PermissionGroupResponse> getAllPermissionGroups(String token);
    PermissionGroupResponse getPermissionGroupById(String token, UUID groupId);
    void assignPermissionToGroup(String token, AssignPermissionToGroupRequest request);
    void removePermissionFromGroup(String token, RemovePermissionFromGroupRequest request);
}
