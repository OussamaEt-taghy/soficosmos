package ma.sofisoft.services.autorisations;
import ma.sofisoft.dtos.autorisations.*;
import java.util.List;
import java.util.UUID;
public interface AutorisationServices {
    void assignPermissionToRole(String token, AssignPermissionToRoleRequest request);
    void removePermissionFromRole(String token, RemovePermissionFromRoleRequest request);
    List<AutorisationResponse> getAutorisationsByRole(String token, String roleId);
    List<AutorisationResponse> getAutorisationsByPermission(String token, UUID permissionId);
}
