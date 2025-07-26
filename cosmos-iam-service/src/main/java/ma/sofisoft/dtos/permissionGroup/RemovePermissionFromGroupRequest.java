package ma.sofisoft.dtos.permissionGroup;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RemovePermissionFromGroupRequest {
    @NotBlank(message = "Permission ID is required")
    private String permissionId;

    @NotBlank(message = "Group ID is required")
    private String groupId;
}
