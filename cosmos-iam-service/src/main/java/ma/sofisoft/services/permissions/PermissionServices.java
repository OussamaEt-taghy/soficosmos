package ma.sofisoft.services.permissions;
import ma.sofisoft.dtos.permissions.*;
import java.util.List;
import java.util.UUID;

public interface PermissionServices {
    UUID createPermission(String token, CreatePermissionRequest request);
    void updatePermission(String token, UUID permissionId, UpdatePermissionRequest request);
    void deletePermission(String token, UUID permissionId);
    List<PermissionResponse> getAllPermissions(String token);
    PermissionResponse getPermissionById(String token, UUID permissionId);
}
