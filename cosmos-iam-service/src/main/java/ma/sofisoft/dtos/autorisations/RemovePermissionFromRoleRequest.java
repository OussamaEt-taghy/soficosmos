package ma.sofisoft.dtos.autorisations;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RemovePermissionFromRoleRequest {
    @NotBlank(message = "Permission ID is required")
    private String permissionId;

    @NotBlank(message = "Role ID is required")
    private String roleId;
}
