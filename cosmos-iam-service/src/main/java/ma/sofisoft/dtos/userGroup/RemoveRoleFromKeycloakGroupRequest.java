package ma.sofisoft.dtos.userGroup;
import lombok.*;
import jakarta.validation.constraints.NotBlank;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RemoveRoleFromKeycloakGroupRequest {
    @NotBlank(message = "Group ID is required")
    private String groupId;

    @NotBlank(message = "Role name is required")
    private String roleName;
}
